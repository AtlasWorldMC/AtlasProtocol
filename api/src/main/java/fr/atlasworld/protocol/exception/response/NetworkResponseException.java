package fr.atlasworld.protocol.exception.response;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.NetworkException;

import java.util.UUID;

public class NetworkResponseException extends NetworkException {

    public NetworkResponseException(int code, UUID communicationIdentifier) {
        super(code, communicationIdentifier);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }

    public NetworkResponseException(String message, int code, UUID communicationIdentifier) {
        super(message, code, communicationIdentifier);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }

    public NetworkResponseException(String message, Throwable cause, int code, UUID communicationIdentifier) {
        super(message, cause, code, communicationIdentifier);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }

    public NetworkResponseException(Throwable cause, int code, UUID communicationIdentifier) {
        super(cause, code, communicationIdentifier);

        Preconditions.checkArgument(code >= 300 && code < 400, "Error code is outside of allowed range! [300; 399]");
    }
}
