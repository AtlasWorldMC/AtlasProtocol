package fr.atlasworld.protocol.exception.request;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.UUID;

/**
 * Thrown when a received payload (or message) is invalid.
 */
public class PayloadInvalidException extends NetworkRequestException {
    public static final int CODE = 203;

    public PayloadInvalidException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public PayloadInvalidException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public PayloadInvalidException(String message, InvalidProtocolBufferException cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public PayloadInvalidException(UUID communicationIdentifier, InvalidProtocolBufferException cause) {
        super(cause, CODE, communicationIdentifier);
    }
}
