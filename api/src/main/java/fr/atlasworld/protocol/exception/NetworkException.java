package fr.atlasworld.protocol.exception;

import java.io.IOException;

/**
 * Generic Network Exception.
 */
public class NetworkException extends IOException {
    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
