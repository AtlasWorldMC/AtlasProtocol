package fr.atlasworld.protocol.handler;

import fr.atlasworld.common.security.Encryptor;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.NetworkTamperedException;
import fr.atlasworld.protocol.exception.request.PacketInvalidException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.security.MessageDigest;

public class ServerHandshakeHandler extends ChannelDuplexHandler {
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final int MIN_PACKET_SIZE = 4;

    private final byte[] serverInfo; // Use pre-parsed value for less calculation when initializing connection.
    private final Encryptor sessionEcryptor;

    private boolean validated;
    private SecretKey key;
    private Encryptor encryptor;
    private Mac signer;

    public ServerHandshakeHandler(byte[] serverInfo, Encryptor sessionEncryptor) {
        this.serverInfo = serverInfo;
        this.sessionEcryptor = sessionEncryptor;

        this.validated = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(this.serverInfo);
        buf.writeInt(this.serverInfo.length);

        ctx.writeAndFlush(buf);
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
        if (this.validated) { // Encrypting & Handshake completed.
            outBuf = ctx.alloc().buffer();
            this.encrypt(buf, outBuf);
        } else {
            outBuf = buf;
        }

        ctx.write(outBuf, promise);
    }

    private void encrypt(ByteBuf in, ByteBuf out) throws NetworkException {
        if (in.readableBytes() < MIN_PACKET_SIZE)
            throw new PacketInvalidException("Packet is too small!", NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        try {
            byte[] encryptedBytes = this.encryptor.encrypt(in.nioBuffer().array());
            byte[] signature = this.signer.doFinal(encryptedBytes);

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

        if (this.validated) { // Encrypting & Handshake completed.
            ByteBuf outBuf = ctx.alloc().buffer();
            this.decrypt(buf, outBuf);
            ctx.fireChannelRead(outBuf);
        }

        this.handleHandshake(buf, ctx);
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

            byte[] actualSignature = this.signer.doFinal(encryptedBytes);
            if (MessageDigest.isEqual(actualSignature, signature))
                throw new NetworkTamperedException("Packet signatures do not match!");

            out.writeBytes(this.encryptor.decrypt(encryptedBytes));
        } catch (Throwable e) {
            out.release(); // Release no longer useful buffer to prevent memory leaks

            if (e instanceof NetworkException)
                throw (NetworkException) e; // Don't catch network exceptions

            throw new PacketInvalidException("Unable to decrypt packet!", e, NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        } finally {
            in.release();
        }
    }

    private void handleHandshake(ByteBuf buf, ChannelHandlerContext ctx) throws NetworkException {

    }
}
