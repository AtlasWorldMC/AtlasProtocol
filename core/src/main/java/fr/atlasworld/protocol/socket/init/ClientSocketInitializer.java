package fr.atlasworld.protocol.socket.init;

import fr.atlasworld.protocol.handler.CodecHandler;
import fr.atlasworld.protocol.handler.ExecutorHandler;
import fr.atlasworld.protocol.handler.HandshakeHandler;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;

public class ClientSocketInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientSocketImpl socket;
    private final KeyFactory factory;
    private final int rateLimit;

    public ClientSocketInitializer(ClientSocketImpl socket, int rateLimit) throws GeneralSecurityException {
        this.socket = socket;
        this.factory = KeyFactory.getInstance(HandshakeHandler.ASYMMETRIC_KEY_ALGORITHM);
        this.rateLimit = rateLimit;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(CodecHandler.MAX_PACKET_SIZE, 0, Integer.BYTES, 0, Integer.BYTES));
        pipeline.addLast(HandshakeHandler.createClient(this.socket, this.factory, this.rateLimit));
        pipeline.addLast(new CodecHandler()); // Decode Requests
        pipeline.addLast(new ExecutorHandler(this.socket, this.socket.registry(), this.socket.rootNode())); // Handles requests
    }
}
