package fr.atlasworld.protocol.event.socket;

import fr.atlasworld.protocol.event.NetworkEvent;
import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

/**
 * Generic Socket Event
 */
public class SocketEvent extends NetworkEvent {
    public SocketEvent(@NotNull Socket socket) {
        super(socket);
    }
}
