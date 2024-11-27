package fr.atlasworld.protocol.exception.request;

import java.util.UUID;

/**
 * Thrown when an unknown request is received.
 */
public class UnknownRequestException extends NetworkRequestException {
    public static final int CODE = 204;

    public UnknownRequestException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public UnknownRequestException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public UnknownRequestException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public UnknownRequestException(Throwable cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
