package fr.atlasworld.protocol.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.PacketToBigException;
import fr.atlasworld.protocol.exception.request.PacketInvalidException;
import fr.atlasworld.protocol.generated.HeaderWrapper;
import fr.atlasworld.protocol.handler.event.HandshakeFinishedEvent;
import fr.atlasworld.protocol.packet.Header;
import fr.atlasworld.protocol.packet.PacketBase;
import fr.atlasworld.protocol.socket.Socket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CodecHandler extends ChannelDuplexHandler {
    public static final short MAX_HEADER_SIZE = 200;

    public static final int MIN_PACKET_SIZE = 4;
    public static final int MAX_PACKET_SIZE = 4194304;

    private BlockingQueue<ByteBuf> packetQueue;
    private ConnectionImpl connection;

    public CodecHandler() {
        this.packetQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof PacketPackage packet)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        byte[] header = packet.header().toByteArray();
        if (header.length > MAX_HEADER_SIZE)
            throw new PacketToBigException("Header exceeds maximum header size (" + MAX_HEADER_SIZE + "): " + header.length,
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);

        byte[] payload = packet.message().toByteArray();
        ByteBuf buffer = ctx.channel().alloc().directBuffer();

        buffer.writeBytes(payload);
        buffer.writeBytes(header);
        buffer.writeShort(header.length);

        if (this.connection == null) {
            this.packetQueue.add(buffer); // Queue packets, prevents from sending packets while the handshake is still going.
            return;
        }

        ctx.write(buffer, promise);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (this.connection == null) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Handshake not finished, but packet was read on channel handler!");
        }

        if (!(msg instanceof ByteBuf buffer)) {
            ReferenceCountUtil.release(msg);
            throw new IllegalArgumentException("Unexpected Packet Type!");
        }

        short headerSize = buffer.readShort();
        if (headerSize > MAX_HEADER_SIZE) {
            buffer.release();
            throw new PacketToBigException("Header exceeds maximum header size (" + MAX_HEADER_SIZE + " bytes): " + headerSize,
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        if (headerSize < 0) { // Packet is invalid or with an unexpected offset.
            buffer.release();
            throw new PacketInvalidException("Header size is invalid!",
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        byte[] headerBytes = new byte[headerSize];
        byte[] payloadBytes = new byte[buffer.readableBytes() - headerSize];

        buffer.readBytes(headerBytes);
        buffer.readBytes(payloadBytes);

        buffer.release(); // No need for the buffer anymore.

        HeaderWrapper.Header header;
        try {
            header = HeaderWrapper.Header.parseFrom(headerBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new PacketInvalidException("Header is not valid!", e,
                    NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        }

        boolean response = header.hasCode();
        if (response && header.hasRequest())
            throw new PacketInvalidException("Header has a response code but also a request entry!",
                    new UUID(header.getIdMostSig(), header.getIdLeastSig()));

        PacketBase packet = new PacketBase(new Header(header, header.hasCode()), this.connection, payloadBytes);
        ctx.fireChannelRead(packet);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof HandshakeFinishedEvent(ConnectionImpl eventConnection)) {
            this.connection = eventConnection;

            ChannelPromise lastWrite = ctx.newPromise();
            while (!this.packetQueue.isEmpty()) {
                ByteBuf buf = this.packetQueue.poll();

                if (this.packetQueue.isEmpty()) { // Last packet.
                    ctx.write(buf, lastWrite);
                    continue;
                }

                ctx.write(buf);
            }

            lastWrite.addListener(future -> {
                ctx.flush();
                this.packetQueue = null; // Lose reference to the queue for GC
            });
        }

        super.userEventTriggered(ctx, event); // Pass to next ChannelHandler
    }
}
