package fr.atlasworld.protocol.exception.response;

/**
 * Thrown when the responder failed due of an external component to handle the request.
 * <b>ex: </b> Database failure or other.
 */
public class ExternalFailureNetworkException extends NetworkResponseException {
    public static final int CODE = 301;

    public ExternalFailureNetworkException() {
        super(CODE);
    }

    public ExternalFailureNetworkException(String message) {
        super(message, CODE);
    }

    public ExternalFailureNetworkException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    public ExternalFailureNetworkException(Throwable cause) {
        super(cause, CODE);
    }
}
