package fr.atlasworld.protocol.event.connection;

import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.socket.ServerSocket;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a connection has been established with a remote.
 * <p>
 * <b>Warning:</b> it's not recommended to interact with the {@link Connection connection} at this state,
 * the connection is far from being fully initialized, all attempt to send packet or to close the connection
 * will result into them being queued.
 * <p>
 * In-case this event happens on the server side, this connection <b>won't</b> be listed in {@link ServerSocket#connections()}!
 * <p>
 * If you need to interact with the connection, it's recommended to use {@link ConnectionValidatedEvent}.
 */
public class ConnectionEstablishedEvent extends ConnectionEvent {
    public ConnectionEstablishedEvent(@NotNull Connection connection) {
        super(connection);
    }
}
