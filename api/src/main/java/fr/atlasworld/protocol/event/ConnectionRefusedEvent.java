package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a connection is refused to connect.
 * <p>
 * This event is also called with {@link ConnectionTerminatedEvent}.
 */
public class ConnectionRefusedEvent extends ConnectionEvent {
    private final Cause cause;

    public ConnectionRefusedEvent(@NotNull Connection connection, @NotNull Cause cause) {
        super(connection);

        Preconditions.checkNotNull(cause);
        this.cause = cause;
    }

    /**
     * Retrieve the cause of the refused connection.
     *
     * @return the cause.
     */
    @NotNull
    public Cause cause() {
        return this.cause;
    }

    /**
     * Enum that contains all the possible causes for a refused connection.
     */
    public static enum Cause {

        /**
         * The connection was refused because of en error or a network failure.
         */
        FAILURE,

        /**
         * Packets during authentication and/or handshake we're invalid;
         */
        INVALID,

        /**
         * A session with these credentials is already connected.
         */
        SESSION_ACTIVE,

        /**
         * Remote failed the challenge.
         */
        CHALLENGE_FAILURE,

        /**
         * The connection was not authorized to connect.
         */
        UNAUTHORIZED,

        /**
         * Used when the connection is blacklisted from connecting.
         */
        BLACKLISTED;
    }
}
