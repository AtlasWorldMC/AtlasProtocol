package fr.atlasworld.protocol.exception.request;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Thrown when the server and client is de-synchronised.
 */
public class NetworkDeSyncException extends NetworkRequestException {
    public static final int CODE = 205;

    public NetworkDeSyncException() {
        super(CODE);
    }

    public NetworkDeSyncException(String message) {
        super(message, CODE);
    }

    public NetworkDeSyncException(String message, InvalidProtocolBufferException cause) {
        super(message, cause, CODE);
    }

    public NetworkDeSyncException(InvalidProtocolBufferException cause) {
        super(cause, CODE);
    }
}
