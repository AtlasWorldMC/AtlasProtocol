package fr.atlasworld.protocol.exception.request;

/**
 * Thrown when an unknown request is received.
 */
public class UnknownRequestException extends NetworkRequestException {
    public static final int CODE = 204;

    public UnknownRequestException() {
        super(CODE);
    }

    public UnknownRequestException(String message) {
        super(message, CODE);
    }

    public UnknownRequestException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    public UnknownRequestException(Throwable cause) {
        super(cause, CODE);
    }
}
