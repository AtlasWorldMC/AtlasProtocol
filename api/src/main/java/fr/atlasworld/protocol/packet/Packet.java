package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.NetworkRequestException;

/**
 * Represents a packet received from remote.
 */
@FunctionalInterface
public interface Packet {

    /**
     * Handle a received packet;
     *
     * @param ctx context for the handling.
     * @param request request received.
     *
     * @throws NetworkException exception thrown if the handling of the request fails.
     */
    void handle(PacketHandlerContext ctx, Request request) throws NetworkException;
}
