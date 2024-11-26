package fr.atlasworld.protocol.connection;

import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.protocol.socket.Socket;
import fr.atlasworld.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
     * Retrieve the public key of the connection.
     *
     * @return public key of the connection.
     */
    @NotNull
    PublicKey publicKey();

    /**
     * Ping (or latency) between the packets being sent and received.
     *
     * @return current ping of the connection, {@code -1} if the connection has been terminated.
     */
    int ping();

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
     * Retrieve the remote address.
     *
     * @return remote address of the connection.
     */
    @NotNull
    InetSocketAddress remoteAddress();

    /**
     * Send a packet to the remote.
     *
     * @param key key of the packet.
     * @param payload payload to be sent within the packet.
     *
     * @return future that will contain the response from remote,
     *         or fail if something went wrong during sending or receiving.
     */
    @NotNull
    <P extends Message> CompletableFuture<Response> sendPacket(@NotNull RegistryKey key, @NotNull P payload);

    /**
     * Disconnects this connection.
     *
     * @param force disconnects the connection even if the disconnection failed,
     *              this will simply interrupt the connection.
     *
     * @return future that will be completed when the connection has been terminated.
     */
    @NotNull
    CompletableFuture<Void> disconnect(boolean force);

    /**
     * Retrieve the socket of the connection.
     *
     * @return socket of the connection.
     */
    @NotNull
    Socket socket();
}
