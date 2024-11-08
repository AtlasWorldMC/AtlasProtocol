package fr.atlasworld.protocol.connection;

import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
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
     * Checks whether the connection group contains unsafe/untrusted connections.
     *
     * @return true if the connection group contains unsafe connections.
     */
    boolean unsafe();

    /**
     * Retrieve the average ping of all connections.
     *
     * @return the average ping.
     */
    int averagePing();

    /**
     * Disconnects all connections in this group.
     *
     * @param force disconnects the connection even if the disconnection failed,
     *              this will simply interrupt the connection.
     *
     * @return future of the disconnecting of all connections.
     */
    CompletableFuture<Void> disconnect(boolean force);

    /**
     * Send a packet to all connections
     *
     * @param key key of the packet.
     * @param payload payload to be sent within the packet.
     * @param response protobuf generated class expected as responses.
     * @param trustedOnly only send this packet to trusted connections.
     *
     * @return a set of future for every remote the packet was sent to.
     */
    <P extends Message, R extends Message> Set<CompletableFuture<Response<R>>> sendPacket(@NotNull RegistryKey key, @NotNull P payload, @NotNull Class<R> response, boolean trustedOnly);

    /**
     * Retrieve all connections in this group.
     *
     * @return all connections in this group.
     */
    Set<Connection> connections();
}
