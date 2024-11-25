package fr.atlasworld.protocol.exception.request;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.NetworkException;

/**
 * Generic Network Request Exception.
 */
public class NetworkRequestException extends NetworkException {

    public NetworkRequestException(int code) {
        super(code);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }

    public NetworkRequestException(String message, int code) {
        super(message, code);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }

    public NetworkRequestException(String message, Throwable cause, int code) {
        super(message, cause, code);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }

    public NetworkRequestException(Throwable cause, int code) {
        super(cause, code);

        Preconditions.checkArgument(code >= 200 && code < 300, "Error code is outside of allowed range! [200; 299]");
    }
}
