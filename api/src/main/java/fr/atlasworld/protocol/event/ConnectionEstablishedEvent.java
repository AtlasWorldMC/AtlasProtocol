package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;

/**
 * Called when a connection is established.
 */
@ThreadSafe
public class ConnectionEstablishedEvent extends ConnectionEvent {
    private final boolean insecureConnection;
    private final Duration handshakeDuration;

    public ConnectionEstablishedEvent(@NotNull Connection connection, boolean insecureConnection, @NotNull Duration handshakeDuration) {
        super(connection);

        Preconditions.checkNotNull(handshakeDuration);

        this.insecureConnection = insecureConnection;
        this.handshakeDuration = handshakeDuration;
    }

    /**
     * Retrieve whether the connection is unsecured.
     * <p>
     * An insecure connection means that the connection has not yet been authenticated.
     *
     * @return true if the connection is unsecure, false otherwise.
     */
    public boolean insecureConnection() {
        return this.insecureConnection;
    }

    /**
     * Retrieve the time it took for the handshake to finish.
     *
     * @return handshake establishing time.
     */
    @NotNull
    public Duration handshakeDuration() {
        return this.handshakeDuration;
    }
}
