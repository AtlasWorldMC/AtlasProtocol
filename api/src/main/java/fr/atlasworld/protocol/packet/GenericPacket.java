package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.packet.header.Header;
import org.jetbrains.annotations.NotNull;

/**
 * Generic Packet, holds the base packets methods.
 *
 * @param <M> message type.
 */
public interface GenericPacket<M extends Message> {

    /**
     * Retrieve the header of the packet.
     *
     * @return the header.
     */
    @NotNull
    Header header();

    /**
     * Retrieve the payload of the packet.
     *
     * @return the payload.
     */
    @NotNull
    M payload();

    /**
     * Retrieve the source of the packet.
     *
     * @return source of the packet.
     */
    @NotNull
    Connection source();
}
