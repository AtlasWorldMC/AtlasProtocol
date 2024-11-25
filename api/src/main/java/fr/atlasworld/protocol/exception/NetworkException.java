package fr.atlasworld.protocol.exception;

import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Generic Network Exception.
 */
public class NetworkException extends IOException {
    private final int code;

    public NetworkException(int code) {
        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");

        this.code = code;
    }

    public NetworkException(String message, int code) {
        super(message);

        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        this.code = code;
    }

    public NetworkException(String message, Throwable cause, int code) {
        super(message, cause);

        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        this.code = code;
    }

    public NetworkException(Throwable cause, int code) {
        super(cause);

        Preconditions.checkArgument(code >= 200, "Network exception is out of range.");
        this.code = code;
    }
}
