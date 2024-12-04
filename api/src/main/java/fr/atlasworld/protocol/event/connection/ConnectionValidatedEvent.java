package fr.atlasworld.protocol.event.connection;

import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a connection has finished its handshake process,
 * and has been fully authenticated and initialized.
 * <p>
 * This event is called <b>after</b> {@link ConnectionEstablishedEvent} that allows to
 * retrieve the connection prematurely.
 */
public class ConnectionValidatedEvent extends ConnectionEvent {
    private final boolean usedCustomAuthentication;

    public ConnectionValidatedEvent(@NotNull Connection connection, boolean usedCustomAuthentication) {
        super(connection);
        this.usedCustomAuthentication = usedCustomAuthentication;
    }

    public final boolean usedCustomAuthentication() {
        return this.usedCustomAuthentication;
    }
}
