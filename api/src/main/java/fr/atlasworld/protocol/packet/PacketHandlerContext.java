package fr.atlasworld.protocol.packet;

import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

/**
 * Packet Handling context, used to handle packets.
 */
public interface PacketHandlerContext {

    /**
     * Retrieve the responder for this request.
     *
     * @return responder for this request.
     */
    Responder responder();

    /**
     * Retrieve the side on which the packet was received.
     *
     * @return side on which the packet was received.
     */
    @NotNull
    Side side();

    /**
     * Retrieve the source of the received packet.
     *
     * @return source of the packet.
     */
    @NotNull
    Connection source();

    /**
     * Retrieve the current socket.
     *
     * @return current socket.
     */
    @NotNull
    Socket socket();
}
