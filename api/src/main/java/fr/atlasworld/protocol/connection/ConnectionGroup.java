package fr.atlasworld.protocol.connection;

import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.connection.ConnectionEvent;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a group of connections.
 * <p>
 * This allows you to create bulk actions on multiple connections.
 */
public interface ConnectionGroup {

    /**
     * Retrieve the connections event node.
     * <p>
     * This event node only calls event related to connections within this group.
     *
     * @return connection's event node.
     */
    EventNode<ConnectionEvent> eventNode();

    /**
     * Retrieve the average ping of all connections.
     * <p>
     * If the ping of a connection is not yet calculated (equals {@code -1}),
     * it won't be added to the average.
     *
     * @see Connection#ping()
     *
     * @return the average ping.
     */
    int averagePing();

    /**
     * Disconnects all connections in this group.
     *
     * @param reason reason for disconnecting.
     *
     * @return future of the disconnecting of all connections,
     *         even if disconnecting gracefully fails the connection will still be closed.
     *
     * @see Connection#disconnect(String)
     */
    CompletableFuture<Void> disconnect(@NotNull String reason);

    /**
     * Send a packet to all connections
     *
     * @param key key of the packet.
     * @param payload payload to be sent within the packet.
     *
     * @return a set of future for every remote the packet was sent to.
     */
    <P extends Message> Set<CompletableFuture<Response>> sendPacket(@NotNull RegistryKey key, @NotNull P payload);

    /**
     * Retrieve all connections in this group.
     *
     * @return all connections in this group.
     */
    Set<Connection> connections();

    /**
     * Retrieve a connection using its identifier.
     *
     * @param identifier identifier of the connection.
     *
     * @return Optional possibly containing the connection,
     *         if the connection is not present it could that it was never present
     *         or that the connection has ended.
     */
    Optional<Connection> retrieveConnection(@NotNull UUID identifier);
}
