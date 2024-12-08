package fr.atlasworld.protocol.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.common.logging.LogUtils;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.event.EarlyNetworkFailureEvent;
import fr.atlasworld.protocol.event.connection.ConnectionExceptionEvent;
import fr.atlasworld.protocol.event.connection.ConnectionRequestReceivedEvent;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.UnknownRequestException;
import fr.atlasworld.protocol.exception.response.FailureNetworkException;
import fr.atlasworld.protocol.generated.AcknowledgementWrapper;
import fr.atlasworld.protocol.generated.EmptyWrapper;
import fr.atlasworld.protocol.handler.event.HandshakeFinishedEvent;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.packet.PacketBase;
import fr.atlasworld.protocol.packet.PacketHandlerContextImpl;
import fr.atlasworld.protocol.packet.ResponderImpl;
import fr.atlasworld.protocol.socket.Socket;
import fr.atlasworld.registry.Registry;
import fr.atlasworld.registry.RegistryKey;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ExecutorHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Socket socket;
    private final Registry<Packet> registry;
    private final EventNode<Event> rootNode;

    private ConnectionImpl connection;

    public ExecutorHandler(Socket socket, Registry<Packet> registry, EventNode<Event> rootNode) {
        this.socket = socket;
        this.registry = registry;
        this.rootNode = rootNode;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof PacketBase packet)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        this.updatePing(packet);
        if (packet.header().isRequestHeader()) {
            this.handleRequest(packet);
            return;
        }

        this.handleResponse(packet);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof HandshakeFinishedEvent(ConnectionImpl eventConnection))
            this.connection = eventConnection;

        super.userEventTriggered(ctx, event); // Pass event to next handler
    }

    private void handleRequest(PacketBase request) throws NetworkException {
        CompletableFuture.runAsync(() ->
                request.source().rootNode().callEvent(new ConnectionRequestReceivedEvent(request.source(), request)));

        RegistryKey key = request.header().request();

        Packet packet = this.registry.retrieveValue(key)
                .orElseThrow(() -> new UnknownRequestException("Unknown request: " + key, request.header().uniqueId()));

        PacketHandlerContextImpl context = request.createHandlingContext();

        try {
            packet.handle(context, request);
        } catch (Throwable cause) {
            if (cause instanceof NetworkException)
                throw cause;

            throw new FailureNetworkException("Request Handling Failed", cause, request.header().uniqueId());
        }
    }

    private void handleResponse(PacketBase response) {
        if (response.header().responseCode() == 0) {
            this.handleAck(response);
            return;
        }

        response.source().handleResponse(response.header().uniqueId(), response);
    }

    private void updatePing(PacketBase packet) {
        int currentPing = Math.round(System.currentTimeMillis() - packet.header().time());
        packet.source().updatePing((currentPing + packet.source().ping()) / 2); // Lowers fluctuation
    }

    private void handleAck(PacketBase ack) {
        UUID identifier = ack.header().uniqueId();
        long timeout;

        try {
            timeout = ack.payload(AcknowledgementWrapper.Acknowledge.class).getTimeout();
        } catch (InvalidProtocolBufferException e) {
            timeout = ResponderImpl.DEFAULT_ACK_TIMEOUT.get(ChronoUnit.MILLIS);
        }

        ack.source().acknowledgeRequest(identifier, timeout);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ConnectionImpl connection = this.connection;
        if (connection == null) {
            this.rootNode.callEvent(new EarlyNetworkFailureEvent(this.socket, (InetSocketAddress) ctx.channel().remoteAddress()));
            ctx.channel().close();
            return;
        }

        CompletableFuture.runAsync(() -> this.rootNode.callEvent(new ConnectionExceptionEvent(connection, cause)));

        if (!(cause instanceof NetworkException netExc))
            return;

        short code = (short) netExc.code();
        UUID id = netExc.identifier();

        PacketPackage failurePacket = PacketPackage.createResponsePackage(id, code, EmptyWrapper.Empty.newBuilder().build());
        ctx.channel().writeAndFlush(failurePacket);
    }
}
