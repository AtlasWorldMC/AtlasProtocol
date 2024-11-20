package fr.atlasworld.protocol.handler;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.generated.HeaderWrapper;
import fr.atlasworld.registry.RegistryKey;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketPackage {
    private final boolean response;
    private final HeaderWrapper.Header header;
    private final Message message;
    private final UUID requestId;

    private PacketPackage(boolean response, @NotNull HeaderWrapper.Header header, @NotNull Message message, @NotNull UUID requestId) {
        Preconditions.checkNotNull(header);
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(requestId);

        this.response = response;
        this.header = header;
        this.message = message;
        this.requestId = requestId;
    }

    public boolean response() {
        return this.response;
    }

    public HeaderWrapper.Header header() {
        return this.header;
    }

    public Message message() {
        return this.message;
    }

    public UUID requestId() {
        return this.requestId;
    }

    public static PacketPackage createRequestPackage(long timeout, RegistryKey key, Message payload) {
        UUID requestId = UUID.randomUUID();

        HeaderWrapper.Header header = HeaderWrapper.Header.newBuilder()
                .setIdLeastSig(requestId.getLeastSignificantBits())
                .setIdMostSig(requestId.getMostSignificantBits())
                .setTime(System.currentTimeMillis())
                .setTimeout(timeout)
                .setRequest(key.toString())
                .build();

        return new PacketPackage(false, header, payload, requestId);
    }

    public static PacketPackage createResponsePackage(UUID requestId, short code, Message payload) {
        HeaderWrapper.Header header = HeaderWrapper.Header.newBuilder()
                .setIdLeastSig(requestId.getLeastSignificantBits())
                .setIdMostSig(requestId.getMostSignificantBits())
                .setTime(System.currentTimeMillis())
                .setCode(code)
                .build();

        return new PacketPackage(true, header, payload, requestId);
    }

    @Override
    public String toString() {
        return "PacketPackage{" +
                "response=" + this.response +
                ", header=" + this.header +
                ", message=" + this.message +
                ", requestId=" + this.requestId +
                '}';
    }
}

