package fr.atlasworld.protocol.event.connection;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Called when a sent request times-out.
 */
public class ConnectionRequestTimeoutEvent extends ConnectionEvent {
    private final Duration timeout;
    private final boolean acknowledged;

    public ConnectionRequestTimeoutEvent(@NotNull Connection connection, @NotNull Duration timeout, boolean acknowledged) {
        super(connection);
        Preconditions.checkNotNull(timeout);

        this.timeout = timeout;
        this.acknowledged = acknowledged;
    }

    /**
     * Retrieve the defined duration before the request timed-out.
     *
     * @return duration before the request timeout.
     */
    public final Duration timeout() {
        return this.timeout;
    }

    /**
     * Checks whether the request was acknowledged.
     * <p>
     * When the request has been acknowledged the timeout for the request extends.
     *
     * @return true if the request was acknowledged.
     */
    public final boolean wasAcknowledged() {
        return this.acknowledged;
    }
}
