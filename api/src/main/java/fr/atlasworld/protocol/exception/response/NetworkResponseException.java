package fr.atlasworld.protocol.exception.response;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.NetworkException;

public class NetworkResponseException extends NetworkException {

    public NetworkResponseException(int code) {
        super(code);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }

    public NetworkResponseException(String message, int code) {
        super(message, code);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }

    public NetworkResponseException(String message, Throwable cause, int code) {
        super(message, cause, code);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }

    public NetworkResponseException(Throwable cause, int code) {
        super(cause, code);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }
}
