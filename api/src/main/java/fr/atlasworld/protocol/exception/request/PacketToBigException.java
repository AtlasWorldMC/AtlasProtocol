package fr.atlasworld.protocol.exception.request;

import java.util.UUID;

/**
 * Thrown when the packet is too big to be sent.
 */
public class PacketToBigException extends PacketInvalidException {
    public PacketToBigException(UUID communicationIdentifier) {
        super(communicationIdentifier);
    }

    public PacketToBigException(String message, UUID communicationIdentifier) {
        super(message, communicationIdentifier);
    }

    public PacketToBigException(String message, Throwable cause, UUID communicationIdentifier) {
        super(message, cause, communicationIdentifier);
    }

    public PacketToBigException(Throwable cause, UUID communicationIdentifier) {
        super(cause, communicationIdentifier);
    }
}
