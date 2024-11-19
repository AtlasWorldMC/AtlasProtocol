package fr.atlasworld.protocol.socket.init;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

public class ServerSocketInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
    }
}
