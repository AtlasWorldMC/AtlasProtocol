package fr.atlasworld.protocol.event.socket;

import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when the socket closes.
 */
public class SocketClosedEvent extends SocketEvent {
    public SocketClosedEvent(@NotNull Socket socket) {
        super(socket);
    }
}
