package fr.atlasworld.protocol.handshake;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.common.security.Encryptor;
import fr.atlasworld.common.security.encryptor.SecretKeyEncryptor;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.event.connection.ConnectionRefusedEvent;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.NetworkDeSyncException;
import fr.atlasworld.protocol.exception.request.UnauthorizedRequestException;
import fr.atlasworld.protocol.generated.HandshakeWrapper;
import fr.atlasworld.protocol.handler.HandshakeHandler;
import fr.atlasworld.protocol.handler.event.HandshakeFinishedEvent;
import fr.atlasworld.protocol.socket.ServerSocketImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.*;
import javax.security.auth.DestroyFailedException;
import java.security.*;
import java.util.Arrays;
import java.util.UUID;

public final class ServerHandshake implements Handshake {
    private int state;

    private SecretKey secretKey;
    private ConnectionImpl connection;

    private Encryptor encryptor;
    private Mac signer;

    private final ServerSocketImpl socket;
    private final KeyGenerator generator;
    private final byte[] serverInfo;

    public ServerHandshake(ServerSocketImpl socket, KeyGenerator generator, byte[] serverInfo) {
        this.state = 1;

        this.socket = socket;
        this.generator = generator;
        this.serverInfo = serverInfo;
    }

    @Override
    public boolean finished() {
        return this.connection != null && this.connection.authenticated();
    }

    @Override
    public void initialize(ChannelHandlerContext ctx) throws NetworkException {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeInt(this.serverInfo.length);
        buf.writeBytes(this.serverInfo);

        ctx.writeAndFlush(buf);
    }

    public void handle(ByteBuf packet, ChannelHandlerContext ctx) throws NetworkException {
        try {
            switch (this.state) {
                case 1 -> this.initialize(packet, ctx);
                case 3 -> this.challenge(packet, ctx);
                default -> throw new NetworkDeSyncException("Unknown network state: " + this.state,
                        NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
            }
        } catch (Throwable e) {
            if (e instanceof NetworkException networkEx)
                throw networkEx;

            throw new UnauthorizedRequestException("Unable to complete handshake!", e, NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        } finally {
            packet.release();
        }
    }

    @Override
    public Encryptor encryptor() {
        return this.encryptor;
    }

    @Override
    public Mac signer() {
        return this.signer;
    }

    // State 1
    private void initialize(ByteBuf packet, ChannelHandlerContext ctx) throws InvalidProtocolBufferException, NetworkException, GeneralSecurityException {
        this.state++;

        byte[] rawData = new byte[packet.readableBytes()];
        packet.readBytes(rawData);

        HandshakeWrapper.Initialize initializePayload = HandshakeWrapper.Initialize.parseFrom(rawData);

        // Fill in identity
        UUID identifier = new UUID(initializePayload.getIdMostSig(), initializePayload.getIdLeastSig());
        boolean customAuthenticator = initializePayload.getCustom();

        this.connection = new ConnectionImpl(ctx.channel(), identifier, this.socket, this.socket.defaultTimeout(),
                customAuthenticator, this.socket.rootNode());

        if (!customAuthenticator) {
            PublicKey publicKey = this.socket.authenticator().authenticate(connection, identifier);
            this.connection.updateKey(publicKey);
            this.invokeChallenge(ctx);
            return;
        }

        // Todo: Custom Authentication
    }

    // State 2
    private void invokeChallenge(ChannelHandlerContext ctx) throws GeneralSecurityException {
        this.state++;

        this.secretKey = this.generator.generateKey();
        byte[] keyBytes = this.secretKey.getEncoded();

        this.encryptor = new SecretKeyEncryptor(this.secretKey);
        this.signer = Mac.getInstance(HandshakeHandler.SIGNATURE_ALGORITHM);
        this.signer.init(this.secretKey);

        Cipher cipher = Cipher.getInstance(this.connection.publicKey().getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, this.connection.publicKey());

        byte[] encryptedBytes = cipher.doFinal(keyBytes);
        HandshakeWrapper.Challenge challenge = HandshakeWrapper.Challenge.newBuilder()
                .setChallenge(ByteString.copyFrom(encryptedBytes)).build();

        byte[] data = challenge.toByteArray();
        ByteBuf buf = ctx.alloc().buffer();

        buf.writeInt(data.length);
        buf.writeBytes(data);

        Arrays.fill(data, (byte) 0x00); // Destroy Encoded key

        ctx.writeAndFlush(buf);
    }

    // State 3
    private void challenge(ByteBuf packet, ChannelHandlerContext ctx) throws InvalidProtocolBufferException, GeneralSecurityException, NetworkException {
        this.state++;

        byte[] rawData = new byte[packet.readableBytes()];
        packet.readBytes(rawData);

        HandshakeWrapper.Challenge challenge = HandshakeWrapper.Challenge.parseFrom(rawData);

        byte[] keyBytes = this.socket.serverEncryptor().decrypt(challenge.getChallenge().toByteArray());
        byte[] actualKeyBytes = this.secretKey.getEncoded();

        if (!MessageDigest.isEqual(keyBytes, actualKeyBytes)) {
            this.connection.refuseConnection(ConnectionRefusedEvent.Cause.CHALLENGE_FAILURE);
            throw new UnauthorizedRequestException("Unauthorized connection attempt!",
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        // Destroy keys
        Arrays.fill(keyBytes, (byte) 0x00);
        Arrays.fill(actualKeyBytes, (byte) 0x00);

        this.sendSuccess(ctx);
    }

    private void sendSuccess(ChannelHandlerContext ctx) {
        this.connection.validate();

        // Notify other handler of the authentication
        ctx.fireUserEventTriggered(new HandshakeFinishedEvent(this.connection));
    }
}
