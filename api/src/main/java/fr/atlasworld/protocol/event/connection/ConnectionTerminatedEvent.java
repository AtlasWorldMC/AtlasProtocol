package fr.atlasworld.protocol.event.connection;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a connection has been terminated, this also includes not yet validated connections.
 */
public class ConnectionTerminatedEvent extends ConnectionEvent {
    private final boolean validated;
    private final Cause disconnectCause;

    @Nullable
    private final String reason;

    public ConnectionTerminatedEvent(@NotNull Connection connection, boolean validated, @NotNull Cause disconnectCause, @Nullable String reason) {
        super(connection);

        Preconditions.checkNotNull(disconnectCause);
        this.validated = validated;
        this.disconnectCause = disconnectCause;
        this.reason = reason;
    }

    /**
     * Checks whether the terminated connection was validated.
     *
     * @return true connection validated, false otherwise.
     */
    public boolean validated() {
        return this.validated;
    }

    /**
     * Retrieve the cause of the termination.
     *
     * @return the cause of the termination.
     */
    @NotNull
    public final Cause cause() {
        return this.disconnectCause;
    }

    /**
     * Retrieve the reason of the disconnection.
     * <p>
     * Only present if {@link #cause()} returns {@link Cause#DISCONNECTED}.
     *
     * @return reason for the disconnection.
     */
    @Nullable
    public final String reason() {
        return this.reason;
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
