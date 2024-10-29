package fr.atlasworld.protocol.connection;

import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.ConnectionEvent;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Represents a current active connection between server and client.
 */
public interface Connection {

    /**
     * Retrieve the connections event node.
     * <p>
     * This event node only calls event related to this connection.
     *
     * @return connection's event node.
     */
    EventNode<ConnectionEvent> eventNode();

    /**
     * Unique identifier of the connection.
     * <p>
     * Depending on how the connection was made,
     * this identifier could also represent the identifier of the client.
     *
     * @return unique identifier of the connection.
     */
    @NotNull
    UUID identifier();

    /**
     * Ping (or latency) between the packets being sent and received.
     *
     * @return current ping of the connection, {@code -1} if the connection has been terminated.
     */
    long ping();

    /**
     * Checks whether the connection is connected.
     * <p>
     * When the connection is no longer connected,
     * it's recommended to drop any reference to it.
     *
     * @return true if the connection is connected, false otherwise.
     */
    boolean connected();

    /**
     * Checks whether the connection is authenticated.
     *
     * @return true if the connected is authenticated, false otherwise.
     */
    boolean authenticated();

    /**
     * Retrieve the remote address.
     *
     * @return remote address of the connection.
     */
    @NotNull
    InetSocketAddress remoteAddress();

    /**
     * Disconnects this connection.
     *
     * @return future that will be completed when the connection has been terminated.
     */
    FutureAction<Void> disconnect();
}
