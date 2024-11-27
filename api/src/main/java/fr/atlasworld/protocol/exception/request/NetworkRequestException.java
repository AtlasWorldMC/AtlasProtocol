package fr.atlasworld.protocol.exception.request;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.NetworkException;

import java.util.UUID;

/**
 * Generic Network Request Exception.
 */
public class NetworkRequestException extends NetworkException {

    public NetworkRequestException(int code, UUID communicationIdentifier) {
        super(code, communicationIdentifier);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }

    public NetworkRequestException(String message, int code, UUID communicationIdentifier) {
        super(message, code, communicationIdentifier);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }

    public NetworkRequestException(String message, Throwable cause, int code, UUID communicationIdentifier) {
        super(message, cause, code, communicationIdentifier);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }

    public NetworkRequestException(Throwable cause, int code, UUID communicationIdentifier) {
        super(cause, code, communicationIdentifier);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }
}
