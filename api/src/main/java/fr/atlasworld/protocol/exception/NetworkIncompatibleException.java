package fr.atlasworld.protocol.exception;

import java.util.UUID;

/**
 * Client-side only exception thrown when the server attempting to connect is incompatible.
 */
public class NetworkIncompatibleException extends NetworkException {
    public static final int CODE = -2;


    public NetworkIncompatibleException() {
        super(CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkIncompatibleException(String message) {
        super(message, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkIncompatibleException(String message, Throwable cause) {
        super(message, cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }

    public NetworkIncompatibleException(Throwable cause) {
        super(cause, CODE, UNDEFINED_COMMUNICATION_IDENTIFIER);
    }
}
