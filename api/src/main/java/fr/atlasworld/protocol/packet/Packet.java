package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;

/**
 * Represents a packet received from remote.
 */
@FunctionalInterface
public interface Packet<M extends Message> {

    /**
     * Handle a received packet;
     *
     * @param ctx context for the handling.
     * @param request request received.
     */
    void handle(PacketHandlerContext ctx, Request<M> request);
}
