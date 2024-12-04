package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

/**
 * Generic Event Group that represents any network related event.
 */
public abstract class NetworkEvent implements Event {
    protected final Socket socket;

    protected NetworkEvent(@NotNull Socket socket) {
        Preconditions.checkNotNull(socket);

        this.socket = socket;
    }

    /**
     * Retrieve the socket on which the event happened.
     *
     * @return socket.
     */
    public final Socket socket() {
        return this.socket;
    }
}
