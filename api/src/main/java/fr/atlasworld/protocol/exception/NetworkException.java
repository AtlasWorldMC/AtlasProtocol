package fr.atlasworld.protocol.exception;

import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Generic Network Exception.
 */
public class NetworkException extends IOException {
    protected final int code;

    public NetworkException(int code) {
        Preconditions.checkArgument(code >= 100, "Error codes may not be in success code range!");

        this.code = code;
    }

    public NetworkException(String message, int code) {
        super(message);

        Preconditions.checkArgument(code >= 100, "Error codes may not be in success code range!");

        this.code = code;
    }
}
