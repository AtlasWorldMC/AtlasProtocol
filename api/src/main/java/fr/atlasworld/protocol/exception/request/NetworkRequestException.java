package fr.atlasworld.protocol.exception.request;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.exception.NetworkException;

public class NetworkRequestException extends NetworkException {
    public NetworkRequestException(int code) {
        super(code);

        Preconditions.checkArgument(200 <= code && code < 300, "Network Request exception code is out of range [200; 299]: %s", code);
    }

    public NetworkRequestException(String message, int code) {
        super(message, code);

        Preconditions.checkArgument(200 <= code && code < 300, "Network Request exception code is out of range [200; 299]: %s", code);
    }
}
