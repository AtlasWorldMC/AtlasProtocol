package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * Network Event, simple group to represent network specific events.
 */
public abstract class ConnectionEvent implements Event {
    protected final Connection connection;

    public ConnectionEvent(@NotNull Connection connection) {
        Preconditions.checkNotNull(connection);

        this.connection = connection;
    }

    /**
     * Connection on which the event happened.
     *
     * @return connection.
     */
    @NotNull
    public Connection connection() {
        return this.connection;
    }
}
