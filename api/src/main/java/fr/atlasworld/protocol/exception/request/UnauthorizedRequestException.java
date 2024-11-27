package fr.atlasworld.protocol.exception.request;

import java.util.UUID;

/**
 * Thrown when a received is unauthorized.
 */
public class UnauthorizedRequestException extends NetworkRequestException {
    public static final int CODE = 201;

    public UnauthorizedRequestException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public UnauthorizedRequestException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public UnauthorizedRequestException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public UnauthorizedRequestException(Throwable cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
