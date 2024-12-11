package fr.atlasworld.protocol.socket.init;

import com.google.protobuf.ByteString;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.generated.HandshakeWrapper;
import fr.atlasworld.protocol.handler.CodecHandler;
import fr.atlasworld.protocol.handler.ExecutorHandler;
import fr.atlasworld.protocol.handler.HandshakeHandler;
import fr.atlasworld.protocol.socket.ServerSocketImpl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.jetbrains.annotations.NotNull;

import javax.crypto.KeyGenerator;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.util.Map;

public class ServerSocketInitializer extends ChannelInitializer<SocketChannel> {
    private final ServerSocketImpl socket;
    private final byte[] precalculatedServerInfo;
    private final KeyGenerator secretKeyGenerator;

    public ServerSocketInitializer(ServerSocketImpl socket, Map<String, String> properties) throws GeneralSecurityException {
        this.socket = socket;

        // Initialize everything now to prevent too much processing for new connections.
        HandshakeWrapper.ServerInfo.Builder info = HandshakeWrapper.ServerInfo.newBuilder()
                .setPublicKey(ByteString.copyFrom(this.socket.sessionKeyPair().getPublic().getEncoded()))
                .setVersion(AtlasProtocol.PROTOCOL_VERSION);

        properties.forEach((key, value) -> {
            info.addProperties(HandshakeWrapper.ServerProperty.newBuilder()
                    .setKey(key).setValue(value).build());
        });

        this.precalculatedServerInfo = info.build().toByteArray();
        this.secretKeyGenerator = KeyGenerator.getInstance(HandshakeHandler.SECRET_KEY_ALGORITHM);
    }

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(CodecHandler.MAX_PACKET_SIZE, 0, 4, 0, 4));
        pipeline.addLast(HandshakeHandler.createServer(this.socket, this.secretKeyGenerator, this.precalculatedServerInfo)); // Handle Handshake
        pipeline.addLast(new CodecHandler()); // Decode Requests
        pipeline.addLast(new ExecutorHandler(this.socket, this.socket.registry(), this.socket.rootNode())); // Handles requests
    }
}
