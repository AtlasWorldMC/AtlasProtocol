package fr.atlasworld.protocol.handler;

import com.google.common.util.concurrent.RateLimiter;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.NetworkTamperedException;
import fr.atlasworld.protocol.exception.RateExceededException;
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

@SuppressWarnings("UnstableApiUsage")
public class HandshakeHandler extends ChannelDuplexHandler {
    public static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    public static final String SECRET_KEY_ALGORITHM = "AES";
    public static final String ASYMMETRIC_KEY_ALGORITHM = "RSA";

    private final Handshake handshake;
    private final RateLimiter limiter;

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
        this.limiter = RateLimiter.create(50); // Max 50 requests per sec
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ApiBridge.LOGGER.trace("Initializing new connection with '{}'..", ctx.channel().remoteAddress());
        this.handshake.initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ApiBridge.LOGGER.trace("Ended connection with '{}'.", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
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
        if (in.readableBytes() < CodecHandler.MIN_PACKET_SIZE)
            throw new PacketInvalidException("Packet is too small!", NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        try {
            byte[] unencryptedBytes = new byte[in.readableBytes()];
            in.readBytes(unencryptedBytes);

            byte[] encryptedBytes = this.handshake.encryptor().encrypt(unencryptedBytes);
            byte[] signature = this.handshake.signer().doFinal(encryptedBytes);

            out.writeInt(encryptedBytes.length + signature.length + Short.BYTES); // Write the total length of the packet
            out.writeShort(signature.length);
            out.writeBytes(signature);
            out.writeBytes(encryptedBytes);
        } catch (Throwable e) {
            out.release(); // Failure release the outgoing buffer

            throw new PacketInvalidException("Unable to encrypt packet!", e, NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        } finally {
            in.release(); // Release the ingoing buffer.
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!this.limiter.tryAcquire()) {
            ReferenceCountUtil.release(msg);
            throw new RateExceededException("Handshake request exceeded allowed values!");
        }

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
            return;
        }

        this.handshake.handle(buf, ctx);
    }

    private void decrypt(ByteBuf in, ByteBuf out) throws NetworkException {
        if (in.readableBytes() < CodecHandler.MIN_PACKET_SIZE)
            throw new PacketInvalidException("Packet is too small!", NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        try {
            int signatureLength = in.readShort();
            byte[] signature = new byte[signatureLength];
            in.readBytes(signature);

            byte[] encryptedBytes = new byte[in.readableBytes()];
            in.readBytes(encryptedBytes);

            byte[] actualSignature = this.handshake.signer().doFinal(encryptedBytes);
            if (!MessageDigest.isEqual(actualSignature, signature))
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
