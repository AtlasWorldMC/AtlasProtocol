package fr.atlasworld.protocol.exception.request;

/**
 * Thrown when a received packet is invalid.
 */
public class PacketInvalidException extends NetworkRequestException {
    public static final int CODE = 200;

    public PacketInvalidException() {
        super(CODE);
    }

    public PacketInvalidException(String message) {
        super(message, CODE);
    }

    public PacketInvalidException(String message, Throwable cause) {
        super(message, cause, CODE);
    }

    public PacketInvalidException(Throwable cause) {
        super(cause, CODE);
    }
}
