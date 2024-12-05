package fr.atlasworld.protocol.exception;

/**
 * Called when a packet has been tampered with, this may be caused by poor network,
 * or if a packet has been modified during sending.
 */
public class NetworkTamperedException extends NetworkException {
    public static final int CODE = -1;

    public NetworkTamperedException() {
        super(CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkTamperedException(String message) {
        super(message, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkTamperedException(String message, Throwable cause) {
        super(message, cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkTamperedException(Throwable cause) {
        super(cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }
}
