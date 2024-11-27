package fr.atlasworld.protocol.exception;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.UUID;

/**
 * Generic Network Exception.
 */
public class NetworkException extends IOException {
    public static final UUID UNDEFINED_COMMUNICATION_IDENTIFIER = new UUID(0L, 0L);

    private final int code;
    private final UUID communicationIdentifier;

    public NetworkException(int code, UUID communicationIdentifier) {
        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        Preconditions.checkNotNull(communicationIdentifier);

        this.code = code;
        this.communicationIdentifier = communicationIdentifier;
    }

    public NetworkException(String message, int code, UUID communicationIdentifier) {
        super(message);

        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        Preconditions.checkNotNull(communicationIdentifier);

        this.code = code;
        this.communicationIdentifier = communicationIdentifier;
    }

    public NetworkException(String message, Throwable cause, int code, UUID communicationIdentifier) {
        super(message, cause);

        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        Preconditions.checkNotNull(communicationIdentifier);

        this.code = code;
        this.communicationIdentifier = communicationIdentifier;
    }

    public NetworkException(Throwable cause, int code, UUID communicationIdentifier) {
        super(cause);

        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        Preconditions.checkNotNull(communicationIdentifier);

        this.code = code;
        this.communicationIdentifier = communicationIdentifier;
    }

    public final int code() {
        return this.code;
    }

    public final UUID identifier() {
        return this.communicationIdentifier;
    }

    @Override
    public String toString() {
        return String.format("NetworkException [code=%d, identifier=%s]: %s",
                this.code, this.communicationIdentifier, getMessage());
    }
}
