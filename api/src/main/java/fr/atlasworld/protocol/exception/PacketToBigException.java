package fr.atlasworld.protocol.exception;

/**
 * Thrown when the packet is too big to be sent.
 */
public class PacketToBigException extends NetworkException {
    public PacketToBigException() {
    }

    public PacketToBigException(String message) {
        super(message);
    }

    public PacketToBigException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketToBigException(Throwable cause) {
        super(cause);
    }
}
