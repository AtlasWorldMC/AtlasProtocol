package fr.atlasworld.protocol.socket.init;

import fr.atlasworld.protocol.handler.CodecHandler;
import fr.atlasworld.protocol.handler.ExecutorHandler;
import fr.atlasworld.protocol.socket.ServerSocketImpl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

public class ServerSocketInitializer extends ChannelInitializer<SocketChannel> {
    private final ServerSocketImpl socket;

    public ServerSocketInitializer(ServerSocketImpl socket) {
        this.socket = socket;
    }

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new CodecHandler(this.socket)); // Decode Requests
        pipeline.addLast(new ExecutorHandler(this.socket, this.socket.registry(), this.socket.rootNode())); // Handles requests
    }
}
