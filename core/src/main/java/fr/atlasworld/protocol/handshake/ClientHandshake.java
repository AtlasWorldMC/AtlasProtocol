package fr.atlasworld.protocol.handshake;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.common.security.Encryptor;
import fr.atlasworld.common.security.encryptor.SecretKeyEncryptor;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.ServerInfoImpl;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.NetworkIncompatibleException;
import fr.atlasworld.protocol.exception.request.NetworkDeSyncException;
import fr.atlasworld.protocol.exception.request.UnauthorizedRequestException;
import fr.atlasworld.protocol.generated.HandshakeWrapper;
import fr.atlasworld.protocol.handler.HandshakeHandler;
import fr.atlasworld.protocol.handler.event.HandshakeFinishedEvent;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public final class ClientHandshake implements Handshake {
    private int state;

    private PublicKey serverKey;
    private SecretKey secretKey;

    private Encryptor encryptor;
    private Mac signer;

    private final ClientSocketImpl socket;
    private final ConnectionImpl connection;

    private final KeyFactory factory;

    public ClientHandshake(ClientSocketImpl socket, KeyFactory factory) {
        this.state = 0;

        this.socket = socket;
        this.connection = socket.connection();

        this.factory = factory;
    }

    @Override
    public boolean finished() {
        return this.connection.authenticated();
    }

    @Override
    public void destroy() {
        try {
            this.secretKey.destroy();
        } catch (DestroyFailedException e) {
            ApiBridge.LOGGER.warn("Failed to destroy inactive connection keys: ", e);
        }
    }

    @Override
    public void initialize(ChannelHandlerContext ctx) throws NetworkException {
        // Do nothing at init
    }

    public void handle(ByteBuf packet, ChannelHandlerContext ctx) throws NetworkException {
        try {
            switch (this.state) {
                case 0 -> this.initialize(packet, ctx);
                case 2 -> this.challenge(packet, ctx);
                case 4 -> this.success(packet, ctx);
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
    public Encryptor encryptor()  {
        return this.encryptor;
    }

    @Override
    public Mac signer() {
        return this.signer;
    }

    // State 0
    private void initialize(ByteBuf packet, ChannelHandlerContext ctx) throws InvalidProtocolBufferException, NetworkException {
        this.state++;

        byte[] rawData = new byte[packet.readableBytes()];
        packet.readBytes(rawData);

        HandshakeWrapper.ServerInfo serverInfo = HandshakeWrapper.ServerInfo.parseFrom(rawData);
        if (AtlasProtocol.PROTOCOL_VERSION != serverInfo.getVersion())
            throw new NetworkIncompatibleException("Protocol version does not match: " +
                    "[current: " + AtlasProtocol.PROTOCOL_VERSION + "; remote: " + serverInfo.getVersion() + "]");

        try {
            ServerInfoImpl info = new ServerInfoImpl(serverInfo, this.factory);
            if (this.socket.resolveCompatibility(info))
                throw new NetworkIncompatibleException("Client and Server are incompatible!");

            this.serverKey = info.key();
        } catch (InvalidKeySpecException e) {
            throw new NetworkIncompatibleException("Unable to parse public key, " +
                    "key specifications may be out-dated!", e);
        }

        this.sendInfo(ctx);
    }

    // State 1
    private void sendInfo(ChannelHandlerContext ctx) {
        this.state++;

        HandshakeWrapper.Initialize initializePacket = HandshakeWrapper.Initialize.newBuilder()
                .setCustom(this.connection.usesCustomAuth())
                .setIdLeastSig(this.connection.identifier().getLeastSignificantBits())
                .setIdMostSig(this.connection.identifier().getMostSignificantBits())
                .setPublicKey(ByteString.copyFrom(this.connection.publicKey().getEncoded()))
                .build();

        byte[] data = initializePacket.toByteArray();
        ByteBuf buf = ctx.alloc().buffer();

        buf.writeBytes(data);
        buf.writeInt(data.length + Integer.BYTES);

        ctx.writeAndFlush(buf);
    }

    // State 2
    private void challenge(ByteBuf packet, ChannelHandlerContext ctx) throws GeneralSecurityException {
        this.state++;

        byte[] rawData = new byte[packet.readableBytes()];
        packet.readBytes(rawData);

        byte[] keyBytes = this.socket.clientEncryptor().decrypt(rawData);
        this.secretKey = new SecretKeySpec(keyBytes, HandshakeHandler.SECRET_KEY_ALGORITHM);

        this.encryptor = new SecretKeyEncryptor(this.secretKey);
        this.signer = Mac.getInstance(HandshakeHandler.SIGNATURE_ALGORITHM);
        this.signer.init(this.secretKey);

        this.sendBackChallenge(ctx);
    }

    // State 3
    private void sendBackChallenge(ChannelHandlerContext ctx) throws GeneralSecurityException {
        this.state++;

        Cipher cipher = Cipher.getInstance(this.serverKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, this.serverKey);

        byte[] encryptedKeyBytes = cipher.doFinal(this.secretKey.getEncoded());

        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(encryptedKeyBytes);
        buf.writeInt(encryptedKeyBytes.length + Integer.BYTES);

        ctx.writeAndFlush(buf);
    }

    // State 4
    private void success(ByteBuf packet, ChannelHandlerContext ctx) {
        this.connection.validate();

        // Notify other handler of the authentication
        ctx.fireUserEventTriggered(new HandshakeFinishedEvent(this.connection));
    }
}
