package fr.atlasworld.protocol.handler;

import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.NetworkRequestException;
import fr.atlasworld.protocol.exception.request.UnknownRequestException;
import fr.atlasworld.protocol.exception.response.NetworkResponseException;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.packet.PacketBase;
import fr.atlasworld.protocol.packet.PacketHandlerContextImpl;
import fr.atlasworld.registry.Registry;
import fr.atlasworld.registry.RegistryKey;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ExecutorHandler extends ChannelInboundHandlerAdapter {
    private final Registry<Packet> registry;

    public ExecutorHandler(Registry<Packet> registry) {
        this.registry = registry;
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

        if (packet.header().isRequestHeader()) {
            this.handleRequest(packet);
            return;
        }

        this.handleResponse(packet);
    }

    private void handleRequest(PacketBase request) throws NetworkException {
        RegistryKey key = request.header().request();
        Packet packet = this.registry.retrieveValue(key)
                .orElseThrow(() -> new UnknownRequestException("Unknown request: " + key));

        PacketHandlerContextImpl context = request.createHandlingContext();

        try {
            packet.handle(context, request);
        }
    }

    private void handleResponse(PacketBase response) throws NetworkResponseException {

    }
}
