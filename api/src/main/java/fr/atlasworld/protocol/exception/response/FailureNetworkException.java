package fr.atlasworld.protocol.exception.response;

import java.util.UUID;

/**
 * Thrown when the responder failed to handle the request.
 */
public class FailureNetworkException extends NetworkResponseException {
    public static final int CODE = 300;

    public FailureNetworkException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public FailureNetworkException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public FailureNetworkException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public FailureNetworkException(Throwable cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
