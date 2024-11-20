package fr.atlasworld.protocol.exception;

public class PacketInvalidException extends NetworkException {
    public PacketInvalidException() {
    }

    public PacketInvalidException(String message) {
        super(message);
    }

    public PacketInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketInvalidException(Throwable cause) {
        super(cause);
    }
}
