package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import fr.atlasworld.protocol.packet.header.RequestHeader;
import org.jetbrains.annotations.NotNull;

/**
 * Packet received by a remote and expects a response.
 *
 * @param <M> message type of payload.
 */
public interface Request extends GenericPacket {

    /**
     * Retrieve the header attached to the header.
     *
     * @return header.
     */
    @NotNull
    @Override
    RequestHeader header();
}
