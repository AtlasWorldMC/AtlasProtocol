package fr.atlasworld.protocol.exception.response;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.NetworkException;

public class NetworkResponseException extends NetworkException {
    public NetworkResponseException(int code) {
        super(code);

        Preconditions.checkArgument(300 <= code && code < 400, "Network Request exception code is out of range [300; 399]: %s", code);
    }

    public NetworkResponseException(String message, int code) {
        super(message, code);

        Preconditions.checkArgument(300 <= code && code < 400, "Network Request exception code is out of range 300; 399]: %s", code);
    }
}
