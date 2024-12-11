package fr.atlasworld.protocol.packet;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.request.UnknownRequestException;
import fr.atlasworld.protocol.generated.HeaderWrapper;
import fr.atlasworld.protocol.packet.header.RequestHeader;
import fr.atlasworld.protocol.packet.header.ResponseHeader;
import fr.atlasworld.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Header implements fr.atlasworld.protocol.packet.header.Header, ResponseHeader, RequestHeader {
    private final HeaderWrapper.Header header;
    private final boolean responseHeader;

    public Header(@NotNull HeaderWrapper.Header header, boolean responseHeader) {
        Preconditions.checkNotNull(header);

        this.header = header;
        this.responseHeader = responseHeader;
    }

    @Override
    public long timeout() {
        if (this.responseHeader)
            throw new UnsupportedOperationException("Only requests headers contains this field!");

        return this.header.getTimeout();
    }

    @Override
    public short responseCode() {
        if (!this.responseHeader)
            throw new UnsupportedOperationException("Only response headers contains this field!");

        return (short) this.header.getCode();
    }

    @Override
    public boolean successResponse() {
        return this.responseCode() >= 100 && this.responseCode() < 200;
    }

    @Override
    public long time() {
        return this.header.getTime();
    }

    @Override
    public @NotNull UUID uniqueId() {
        return new UUID(this.header.getIdMostSig(), this.header.getIdLeastSig());
    }

    @Override
    public @NotNull RegistryKey request() throws UnknownRequestException {
        if (this.responseHeader)
            throw new UnsupportedOperationException("Only requests headers contains this field!");

        return RegistryKey.fromString(this.header.getRequest()).orElseThrow(() ->
                new UnknownRequestException("Unknown request: " + this.header.getRequest(),
                        new UUID(this.header.getIdMostSig(), this.header.getIdLeastSig())));
    }

    public boolean isResponseHeader() {
        return this.responseHeader;
    }

    public boolean isRequestHeader() {
        return !this.responseHeader;
    }

    @Override
    public String toString() {
        return this.header.toString();
    }

    public void copyTo(@NotNull HeaderWrapper.Header.Builder builder) {
        Preconditions.checkNotNull(builder);

        builder.setTime(this.header.getTime());
        builder.setIdMostSig(this.header.getIdMostSig());
        builder.setIdLeastSig(this.header.getIdLeastSig());

        builder.setRequest(this.header.getRequest());
        builder.setCode(this.header.getCode());
        builder.setTimeout(this.header.getTimeout());
    }
}
