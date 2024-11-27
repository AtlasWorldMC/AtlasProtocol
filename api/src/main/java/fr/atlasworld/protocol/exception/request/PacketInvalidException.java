package fr.atlasworld.protocol.exception.request;

import java.util.UUID;

/**
 * Thrown when a received packet is invalid.
 */
public class PacketInvalidException extends NetworkRequestException {
    public static final int CODE = 200;

    public PacketInvalidException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public PacketInvalidException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public PacketInvalidException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public PacketInvalidException(Throwable cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
