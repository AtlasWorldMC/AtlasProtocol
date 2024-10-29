package fr.atlasworld.protocol.packet;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.generated.HeaderWrapper;
import fr.atlasworld.protocol.packet.header.RequestHeader;
import fr.atlasworld.protocol.packet.header.ResponseHeader;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Header implements ResponseHeader, RequestHeader {
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
        // TODO: Define Codes
        return false;
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
    public @NotNull String request() {
        if (this.responseHeader)
            throw new UnsupportedOperationException("Only requests headers contains this field!");

        return this.header.getRequest();
    }

    public boolean isResponseHeader() {
        return this.responseHeader;
    }

    public boolean isRequestHeader() {
        return !this.responseHeader;
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
