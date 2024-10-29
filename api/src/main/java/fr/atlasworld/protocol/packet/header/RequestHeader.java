package fr.atlasworld.protocol.packet.header;

import org.jetbrains.annotations.NotNull;

/**
 * Request specific header.
 */
public interface RequestHeader extends Header {

    /**
     * Timeout before the request is cancelled.
     *
     * @return timeout.
     */
    long timeout();

    /**
     * Retrieve the request key.
     *
     * @return request key.
     */
    @NotNull
    String request();
}
