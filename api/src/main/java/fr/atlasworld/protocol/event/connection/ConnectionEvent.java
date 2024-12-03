package fr.atlasworld.protocol.event.connection;

import com.google.common.base.Preconditions;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.event.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Network Event, simple group to represent network specific events.
 */
public abstract class ConnectionEvent extends NetworkEvent {
    protected final Connection connection;

    public ConnectionEvent(@NotNull Connection connection) {
        super(connection.socket()); // Throw NullPointerException if connection is null.
        this.connection = connection;
    }

    /**
     * Connection on which the event happened.
     *
     * @return connection.
     */
    @NotNull
    public final Connection connection() {
        return this.connection;
    }
}
