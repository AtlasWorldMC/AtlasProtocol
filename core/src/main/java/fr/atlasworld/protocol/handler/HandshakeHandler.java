package fr.atlasworld.protocol.handler;

import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.NetworkTamperedException;
import fr.atlasworld.protocol.exception.request.PacketInvalidException;
import fr.atlasworld.protocol.handshake.ClientHandshake;
import fr.atlasworld.protocol.handshake.Handshake;
import fr.atlasworld.protocol.handshake.ServerHandshake;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import fr.atlasworld.protocol.socket.ServerSocketImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.KeyGenerator;
import java.security.KeyFactory;
import java.security.MessageDigest;

public class HandshakeHandler extends ChannelDuplexHandler {
    public static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    public static final String SECRET_KEY_ALGORITHM = "AES";
    public static final String ASYMMETRIC_KEY_ALGORITHM = "RSA";

    private static final int MIN_PACKET_SIZE = 4;

    private final Handshake handshake;

    public static HandshakeHandler createServer(ServerSocketImpl socket, KeyGenerator generator, byte[] serverInfo) {
        ServerHandshake handshake = new ServerHandshake(socket, generator, serverInfo);
        return new HandshakeHandler(handshake);
    }

    public static HandshakeHandler createClient(ClientSocketImpl socket, KeyFactory factory) {
        ClientHandshake handshake = new ClientHandshake(socket, factory);
        return new HandshakeHandler(handshake);
    }

    private HandshakeHandler(Handshake handshake) {
        this.handshake = handshake;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.handshake.initialize(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.handshake.destroy(); // Destroy data
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof ByteBuf buf)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        ByteBuf outBuf;
        if (this.handshake.finished()) { // Encrypting & Handshake completed.
            outBuf = ctx.alloc().buffer();
            this.encrypt(buf, outBuf);
        } else {
            outBuf = buf; // Write as is
        }

        ctx.write(outBuf, promise);
    }

    private void encrypt(ByteBuf in, ByteBuf out) throws NetworkException {
        if (in.readableBytes() < MIN_PACKET_SIZE)
            throw new PacketInvalidException("Packet is too small!", NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        try {
            byte[] unencryptedBytes = new byte[in.readableBytes()];
            in.readBytes(unencryptedBytes);

            byte[] encryptedBytes = this.handshake.encryptor().encrypt(unencryptedBytes);
            byte[] signature = this.handshake.signer().doFinal(encryptedBytes);

            out.writeBytes(encryptedBytes);
            out.writeBytes(signature);
            out.writeShort(signature.length);
            out.writeInt(encryptedBytes.length + signature.length + Short.BYTES + Integer.BYTES); // Write the total length of the packet
        } catch (Throwable e) {
            out.release(); // Failure release the outgoing buffer

            throw new PacketInvalidException("Unable to encrypt packet!", e, NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        } finally {
            in.release(); // Release the ingoing buffer.
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof ByteBuf buf)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        if (this.handshake.finished()) { // Encrypting & Handshake completed.
            ByteBuf outBuf = ctx.alloc().buffer();
            this.decrypt(buf, outBuf);
            ctx.fireChannelRead(outBuf);
        }

        this.handshake.handle(buf, ctx);
    }

    private void decrypt(ByteBuf in, ByteBuf out) throws NetworkException {
        if (in.readableBytes() < MIN_PACKET_SIZE)
            throw new PacketInvalidException("Packet is too small!", NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        try {
            int signatureLength = in.readShort();
            byte[] signature = new byte[signatureLength];
            in.readBytes(signature);

            byte[] encryptedBytes = new byte[in.readableBytes()];
            in.readBytes(encryptedBytes);

            byte[] actualSignature = this.handshake.signer().doFinal(encryptedBytes);
            if (MessageDigest.isEqual(actualSignature, signature))
                throw new NetworkTamperedException("Packet signatures do not match!");

            out.writeBytes(this.handshake.encryptor().decrypt(encryptedBytes));
        } catch (Throwable e) {
            out.release(); // Release no longer useful buffer to prevent memory leaks

            if (e instanceof NetworkException)
                throw (NetworkException) e; // Don't catch network exceptions

            throw new PacketInvalidException("Unable to decrypt packet!", e, NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        } finally {
            in.release();
        }
    }
}
