package fr.atlasworld.protocol.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.NetworkDeSyncException;
import fr.atlasworld.protocol.exception.request.PacketToBigException;
import fr.atlasworld.protocol.exception.request.PacketInvalidException;
import fr.atlasworld.protocol.generated.HeaderWrapper;
import fr.atlasworld.protocol.packet.Header;
import fr.atlasworld.protocol.packet.PacketBase;
import fr.atlasworld.protocol.socket.ClientSocket;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import fr.atlasworld.protocol.socket.ServerSocketImpl;
import fr.atlasworld.protocol.socket.Socket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CodecHandler extends ChannelDuplexHandler {
    private static final short MAX_HEADER_SIZE = 200;

    private final Socket socket;

    public CodecHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof PacketPackage packet)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        byte[] header = packet.header().toByteArray();
        if (header.length > MAX_HEADER_SIZE)
            throw new PacketToBigException("Header exceeds maximum header size (" + MAX_HEADER_SIZE + "): " + header.length,
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        byte[] payload = packet.message().toByteArray();
        ByteBuf buffer = ctx.channel().alloc().directBuffer();

        buffer.writeBytes(payload);
        buffer.writeBytes(header);
        buffer.writeShort(header.length);

        ctx.write(buffer, promise);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof ByteBuf buffer)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        short headerSize = buffer.readShort();
        if (headerSize > MAX_HEADER_SIZE) {
            buffer.release();
            throw new PacketToBigException("Header exceeds maximum header size (" + MAX_HEADER_SIZE + " bytes): " + headerSize,
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        if (headerSize < 0) { // Packet is invalid or with an unexpected offset.
            buffer.release();
            throw new PacketInvalidException("Header size is invalid!",
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        byte[] headerBytes = new byte[headerSize];
        byte[] payloadBytes = new byte[buffer.readableBytes() - headerSize];

        buffer.readBytes(headerBytes);
        buffer.readBytes(payloadBytes);

        buffer.release(); // No need for the buffer anymore.

        HeaderWrapper.Header header;
        try {
            header = HeaderWrapper.Header.parseFrom(headerBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new PacketInvalidException("Header is not valid!", e,
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        boolean response = header.hasCode();
        if (response && header.hasRequest())
            throw new PacketInvalidException("Header has a response code but also a request entry!",
                    new UUID(header.getIdMostSig(), header.getIdLeastSig()));

        ConnectionImpl connection = this.connection(ctx.channel(), new UUID(header.getIdMostSig(), header.getIdLeastSig()));

        PacketBase packet = new PacketBase(new Header(header, header.hasCode()), connection, payloadBytes);
        ctx.fireChannelRead(packet);
    }

    private ConnectionImpl connection(Channel channel, UUID communicationIdentifier) throws NetworkException {
        UUID identifier = channel.attr(ApiBridge.UNIQUE_ID_ATTRIBUTE).get();
        if (identifier == null)
            throw new NetworkDeSyncException("Missing identifier attribute!", communicationIdentifier);

        if (this.socket instanceof ServerSocketImpl serverSocket)
            return (ConnectionImpl) serverSocket.globalConnectionGroup().retrieveConnection(identifier)
                    .orElseThrow(() -> new NetworkDeSyncException("Missing or disconnected connection!", communicationIdentifier));

        return ((ClientSocketImpl) this.socket).connection();
    }
}
