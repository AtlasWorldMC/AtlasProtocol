package fr.atlasworld.protocol.exception.response;

import java.util.UUID;

/**
 * Thrown when the responder failed due of an external component to handle the request.
 * <b>ex: </b> Database failure or other.
 */
public class ExternalFailureNetworkException extends NetworkResponseException {
    public static final int CODE = 301;

    public ExternalFailureNetworkException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public ExternalFailureNetworkException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public ExternalFailureNetworkException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public ExternalFailureNetworkException(Throwable cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
