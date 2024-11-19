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
    private final boolean customAuth;
    private final Duration handshakeDuration;

    public ConnectionEstablishedEvent(@NotNull Connection connection, boolean customAuth, @NotNull Duration handshakeDuration) {
        super(connection);

        Preconditions.checkNotNull(handshakeDuration);

        this.customAuth = customAuth;
        this.handshakeDuration = handshakeDuration;
    }

    /**
     * Retrieve whether the connection was authenticated using a custom authenticator.
     * <p>
     * The custom authenticator means that it wasn't authenticated using the traditional way.
     *
     * @return true if the connection was authenticated using a custom authenticator, false otherwise.
     */
    public boolean customAuth() {
        return this.customAuth;
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
