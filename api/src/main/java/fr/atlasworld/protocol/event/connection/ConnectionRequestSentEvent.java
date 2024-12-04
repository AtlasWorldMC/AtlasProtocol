package fr.atlasworld.protocol.event.connection;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.packet.Request;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a request is sent to a remote.
 */
public class ConnectionRequestSentEvent extends ConnectionEvent {
    public final Request request;

    public ConnectionRequestSentEvent(@NotNull Connection connection, @NotNull Request request) {
        super(connection);
        Preconditions.checkNotNull(request);

        this.request = request;
    }

    /**
     * Retrieve the request.
     *
     * @return the request.
     */
    @NotNull
    public final Request request() {
        return this.request;
    }
}
