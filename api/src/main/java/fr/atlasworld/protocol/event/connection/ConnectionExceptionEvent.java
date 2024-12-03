package fr.atlasworld.protocol.event.connection;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when an exception happens on the connection.
 */
public class ConnectionExceptionEvent extends ConnectionEvent {
    private final Throwable cause;

    public ConnectionExceptionEvent(@NotNull Connection connection, @NotNull Throwable cause) {
        super(connection);

        Preconditions.checkNotNull(cause);

        this.cause = cause;
    }

    public Throwable cause() {
        return this.cause;
    }
}
