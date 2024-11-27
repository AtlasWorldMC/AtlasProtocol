package fr.atlasworld.protocol.exception.request;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.UUID;

/**
 * Thrown when the server and client is de-synchronised.
 */
public class NetworkDeSyncException extends NetworkRequestException {
    public static final int CODE = 205;

    public NetworkDeSyncException(UUID communicationIdentifier) {
        super(CODE, communicationIdentifier);
    }

    public NetworkDeSyncException(String message, UUID communicationIdentifier) {
        super(message, CODE, communicationIdentifier);
    }

    public NetworkDeSyncException(String message, InvalidProtocolBufferException cause, UUID communicationIdentifier) {
        super(message, cause, CODE, communicationIdentifier);
    }

    public NetworkDeSyncException(InvalidProtocolBufferException cause, UUID communicationIdentifier) {
        super(cause, CODE, communicationIdentifier);
    }
}
