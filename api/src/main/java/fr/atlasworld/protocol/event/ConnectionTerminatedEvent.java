package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * Event Called when the connection has been terminated.
 */
public class ConnectionTerminatedEvent extends ConnectionEvent {
    private final Cause disconnectCause;

    public ConnectionTerminatedEvent(@NotNull Connection connection, @NotNull Cause disconnectCause) {
        super(connection);

        Preconditions.checkNotNull(disconnectCause);
        this.disconnectCause = disconnectCause;
    }

    /**
     * Retrieve the cause of the termination.
     *
     * @return the cause of the termination.
     */
    @NotNull
    public Cause cause() {
        return this.disconnectCause;
    }

    /**
     * Enum that contains all possible causes of a disconnection.
     */
    public static enum Cause {

        /**
         * The connection has timed-out.
         */
        TIMED_OUT,

        /**
         * The connection was unexpectedly terminated, by the network or by the remote.
         */
        INTERRUPTED,

        /**
         * Called when the connection was refused,
         * in that case a {@link ConnectionRefusedEvent} is also called for more details.
         */
        REFUSED,

        /**
         * The remote gracefully terminated the connection by disconnecting properly.
         */
        DISCONNECTED;
    }
}
