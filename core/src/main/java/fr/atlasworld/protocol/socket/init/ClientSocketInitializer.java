package fr.atlasworld.protocol.socket.init;

import fr.atlasworld.protocol.handler.CodecHandler;
import fr.atlasworld.protocol.handler.ExecutorHandler;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ClientSocketInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientSocketImpl socket;

    public ClientSocketInitializer(ClientSocketImpl socket) {
        this.socket = socket;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new CodecHandler(this.socket)); // Decode Requests
        pipeline.addLast(new ExecutorHandler(this.socket.registry())); // Handles requests
    }
}
