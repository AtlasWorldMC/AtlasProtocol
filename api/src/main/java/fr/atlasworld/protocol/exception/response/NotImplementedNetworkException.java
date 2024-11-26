package fr.atlasworld.protocol.exception.response;

/**
 * Thrown when the request is not yet implemented, this should only be called into a development environment.
 */
public class NotImplementedNetworkException extends NetworkResponseException {
    public static final int CODE = 302;

    public NotImplementedNetworkException() {
        super(CODE);
    }

    public NotImplementedNetworkException(String message) {
        super(message, CODE);
    }

    public NotImplementedNetworkException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    public NotImplementedNetworkException(Throwable cause) {
        super(cause, CODE);
    }
}
