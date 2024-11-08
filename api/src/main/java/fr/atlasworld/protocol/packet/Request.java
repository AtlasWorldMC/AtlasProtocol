package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import fr.atlasworld.protocol.packet.header.RequestHeader;
import org.jetbrains.annotations.NotNull;

/**
 * Packet received by a remote and expects a response.
 *
 * @param <M> message type of payload.
 */
public interface Request<M extends Message> extends GenericPacket<M> {

    /**
     * Retrieve the header attached to the header.
     *
     * @return header.
     */
    @NotNull
    @Override
    RequestHeader header();
}
