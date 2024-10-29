package fr.atlasworld.protocol.packet.header;

import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Generic Universal Header, for request or response.
 */
public interface Header {

    /**
     * Retrieve the time when the packet was sent.
     * <br>
     * Calculating ping with this time field is not recommended,
     * use {@link Connection#ping()} instead.
     *
     * @return time when the packet was sent.
     */
    long time();

    /**
     * Retrieve the unique identifier of the request.
     *
     * @return unique identifier of the request.
     */
    @NotNull
    UUID uniqueId();
}
