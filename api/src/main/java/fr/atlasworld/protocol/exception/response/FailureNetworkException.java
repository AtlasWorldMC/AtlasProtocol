package fr.atlasworld.protocol.exception.response;

/**
 * Thrown when the responder failed to handle the request.
 */
public class FailureNetworkException extends NetworkResponseException {
    public static final int CODE = 300;

    public FailureNetworkException() {
        super(CODE);
    }

    public FailureNetworkException(String message) {
        super(message, CODE);
    }

    public FailureNetworkException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    public FailureNetworkException(Throwable cause) {
        super(cause, CODE);
    }
}
