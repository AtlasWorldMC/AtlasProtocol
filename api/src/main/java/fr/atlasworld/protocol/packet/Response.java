package fr.atlasworld.protocol.packet;

import fr.atlasworld.protocol.packet.header.ResponseHeader;
import org.jetbrains.annotations.NotNull;

/**
 * Packet sent back as a response to a request.
 */
public interface Response extends GenericPacket {

    /**
     * Retrieve the header of the response.
     *
     * @return header.
     */
    @NotNull
    @Override
    ResponseHeader header();
}
