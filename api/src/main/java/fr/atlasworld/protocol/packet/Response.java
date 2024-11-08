package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import fr.atlasworld.protocol.packet.header.ResponseHeader;
import org.jetbrains.annotations.NotNull;

/**
 * Packet sent back as a response to a request.
 */
public interface Response<M extends Message> extends GenericPacket<M> {

    /**
     * Retrieve the header of the response.
     *
     * @return header.
     */
    @NotNull
    @Override
    ResponseHeader header();
}
