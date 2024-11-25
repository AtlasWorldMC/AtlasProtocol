package fr.atlasworld.protocol.exception.request;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Thrown when a received payload (or message) is invalid.
 */
public class PayloadInvalidException extends NetworkRequestException {
    public static final int CODE = 203;

    public PayloadInvalidException() {
        super(CODE);
    }

    public PayloadInvalidException(String message) {
        super(message, CODE);
    }

    public PayloadInvalidException(String message, InvalidProtocolBufferException cause) {
        super(message, cause, CODE);
    }

    public PayloadInvalidException(InvalidProtocolBufferException cause) {
        super(cause, CODE);
    }
}
