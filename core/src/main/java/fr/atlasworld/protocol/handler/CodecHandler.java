package fr.atlasworld.protocol.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.protocol.exception.PacketInvalidException;
import fr.atlasworld.protocol.exception.PacketToBigException;
import fr.atlasworld.protocol.generated.HeaderWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

public class CodecHandler extends ChannelDuplexHandler {
    private static final short MAX_HEADER_SIZE = 200;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof PacketPackage packet)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        byte[] header = packet.header().toByteArray();
        if (header.length > MAX_HEADER_SIZE)
            throw new PacketToBigException("Header exceeds maximum header size (" + MAX_HEADER_SIZE + "): " + header.length);

        byte[] payload = packet.message().toByteArray();
        ByteBuf buffer = ctx.channel().alloc().directBuffer();

        buffer.writeBytes(payload);
        buffer.writeBytes(header);
        buffer.writeShort(header.length);

        ctx.write(buffer, promise);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (!ctx.channel().isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (!(msg instanceof ByteBuf buffer)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        short headerSize = buffer.readShort();
        if (headerSize > MAX_HEADER_SIZE) {
            buffer.release();
            throw new PacketToBigException("Header exceeds maximum header size (" + MAX_HEADER_SIZE + "): " + headerSize);
        }

        if (headerSize < 0) { // Packet is invalid or with an unexpected offset.
            buffer.release();
            throw new PacketInvalidException("Header size is invalid!");
        }

        byte[] headerBytes = new byte[headerSize];
        byte[] payloadBytes = new byte[buffer.readableBytes() - headerSize];

        buffer.readBytes(headerBytes);
        buffer.readBytes(payloadBytes);

        HeaderWrapper.Header header;
        try {
            header = HeaderWrapper.Header.parseFrom(headerBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new PacketInvalidException("Header is not valid!", e);
        }


    }
}
