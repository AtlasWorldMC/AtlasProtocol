package fr.atlasworld.protocol.event.socket;

import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a socket is opened.
 */
public class SocketOpenedEvent extends SocketEvent {
    public SocketOpenedEvent(@NotNull Socket socket) {
        super(socket);
    }
}
