package fr.atlasworld.protocol.exception.response;

import java.util.UUID;

/**
 * Thrown when the request is not yet implemented, this should only be called into a development environment.
 */
public class NotImplementedNetworkException extends NetworkResponseException {
    public static final int CODE = 302;

    public NotImplementedNetworkException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public NotImplementedNetworkException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public NotImplementedNetworkException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public NotImplementedNetworkException(Throwable cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
