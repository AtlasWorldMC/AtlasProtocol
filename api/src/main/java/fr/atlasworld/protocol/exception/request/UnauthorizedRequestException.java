package fr.atlasworld.protocol.exception.request;

/**
 * Thrown when a received is unauthorized.
 */
public class UnauthorizedRequestException extends NetworkRequestException {
    public static final int CODE = 201;

    public UnauthorizedRequestException() {
        super(CODE);
    }

    public UnauthorizedRequestException(String message) {
        super(message, CODE);
    }

    public UnauthorizedRequestException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    public UnauthorizedRequestException(Throwable cause) {
        super(cause, CODE);
    }
}
