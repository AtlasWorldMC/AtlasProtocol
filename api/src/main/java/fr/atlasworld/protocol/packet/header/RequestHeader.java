package fr.atlasworld.protocol.packet.header;

import fr.atlasworld.protocol.exception.request.UnknownRequestException;
import fr.atlasworld.registry.RegistryKey;
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
     *
     * @throws UnknownRequestException if the request key could not be parsed.
     */
    @NotNull
    RegistryKey request() throws UnknownRequestException;
}
