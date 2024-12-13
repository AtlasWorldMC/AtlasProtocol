package fr.atlasworld.protocol.exception;

public class NetworkRateLimitedException extends NetworkException {
    public static final int CODE = -3;

    public NetworkRateLimitedException() {
        super(CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkRateLimitedException(String message) {
        super(message, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkRateLimitedException(String message, Throwable cause) {
        super(message, cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkRateLimitedException(Throwable cause) {
        super(cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }
}
