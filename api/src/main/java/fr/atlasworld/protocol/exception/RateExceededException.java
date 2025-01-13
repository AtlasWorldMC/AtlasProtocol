package fr.atlasworld.protocol.exception;

import java.util.UUID;

/**
 * Thrown when a connection exceeds its rate limit.
 */
public class RateExceededException extends NetworkException {
    public static final int CODE = -3;

    public RateExceededException() {
        super(CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public RateExceededException(String message) {
        super(message, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public RateExceededException(String message, Throwable cause) {
        super(message, cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public RateExceededException(Throwable cause) {
        super(cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }
}
