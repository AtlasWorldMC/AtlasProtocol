package fr.atlasworld.protocol.socket;

import fr.atlasworld.common.annotation.OptionalBuilderArgument;
import fr.atlasworld.common.annotation.RequiredBuilderArgument;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.connection.ConnectionGroup;
import fr.atlasworld.protocol.exception.NetworkRateLimitedException;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.security.Authenticator;
import fr.atlasworld.protocol.security.HandshakeHandler;
import fr.atlasworld.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.time.Duration;

/**
 * Server-Side socket, handles all the logic of the protocol.
 */
public interface ServerSocket extends Socket {

    /**
     * Retrieve all currently connected connections.
     * <p>
     * This does not include not yet validated connections.
     *
     * @return all currently connected and validated connections.
     */
    @NotNull
    ConnectionGroup connections();

    /**
     * Retrieve the current session key-pair.
     *
     * @return current session key-pair.
     */
    @NotNull
    KeyPair sessionKeyPair();

    /**
     * Builder to create a new {@link ServerSocket}.
     */
    interface Builder {

        /**
         * Create a new builder of a {@link ServerSocket}.
         *
         * @return newly created builder.
         */
        static ServerSocket.Builder create() {
            return AtlasProtocol.createServer();
        }

        /**
         * Sets the server bind address.
         * <p>
         * Default: 0.0.0.0:27767
         *
         * @param address address to bind.
         */
        @OptionalBuilderArgument
        Builder bindAddress(@NotNull InetSocketAddress address);

        /**
         * Sets the server authenticator.
         *
         * @param authenticator authenticator that will be used for authentication.
         */
        @RequiredBuilderArgument
        Builder authenticator(@NotNull Authenticator authenticator);

        /**
         * Define the root node, where socket events will be notified.
         *
         * @param rootNode rootNode to notify.
         *
         * @throws IllegalArgumentException if {@code rootNode}
         * is not the root node of the tree.
         */
        @RequiredBuilderArgument
        Builder rootNode(@NotNull EventNode<Event> rootNode);

        /**
         * Sets a custom handshake handler.
         *
         * @param handler handshake handler.
         */
        @OptionalBuilderArgument
        Builder handleHandshake(@NotNull HandshakeHandler handler);

        /**
         * Sets the packet registry.
         *
         * @param registry registry containing the packets.
         */
        @RequiredBuilderArgument
        Builder registry(@NotNull Registry<Packet> registry);

        /**
         * Sets the packet rate-limit <b>per second</b>, if this exceeds this limit the packet will be ignored and
         * {@link NetworkRateLimitedException} will be thrown.
         * <p>
         * <b>Warning: </b> We speak about packets not requests,
         * this includes acknowledgement, requests and response packets.
         * <p>
         * <b>Default: </b> 50 packets per seconds.
         * <br>
         *
         * @param rateLimit amount of packets that can be sent per seconds.
         *
         * @throws IllegalArgumentException if {@code rateLimit} is negative or zero.
         */
        @OptionalBuilderArgument
        Builder rateLimit(int rateLimit);

        /**
         * Adds a property to the handshake properties.
         *
         * @param key key of the property.
         * @param value value of the property.
         */
        @OptionalBuilderArgument
        Builder handshakeProperties(@NotNull String key, @NotNull String value);

        /**
         * Sets the timeout for a request to be responded to.
         * <p>
         * <b>Default:</b> 5secs.
         *
         * @param timeout duration before the requests time out.
         *
         * @throws IllegalArgumentException if the specified {@code timeout} duration is lower than 1.
         */
        @OptionalBuilderArgument
        Builder requestTimeout(@NotNull Duration timeout);

        /**
         * Sets the connection timeout.
         * <p>
         * <b>Default:</b> Operating Specific setting.
         *
         * @param timeout duration before the requests time out.
         *
         * @throws IllegalArgumentException if the specified {@code timeout} duration is lower than 1.
         */
        @OptionalBuilderArgument
        Builder connectTimeout(@NotNull Duration timeout);

        /**
         * Sets the sessions key pair.
         * <p>
         * Default: Generated Key Pair.
         *
         * @param pair keypair.
         */
        @OptionalBuilderArgument
        Builder keyPair(@NotNull KeyPair pair);

        /**
         * Whether the connection should stay alive.
         * <p>
         * Default: true
         */
        @OptionalBuilderArgument
        Builder keepAlive(boolean keepAlive);

        /**
         * Whether to disable Nagle's Algorithm.
         * This can lower the latency but at cost of bandwidth.
         * <p>
         * Default: false
         */
        @OptionalBuilderArgument
        Builder enableNoDelay(boolean noDelay);

        /**
         * Sets the pending connection queue max size.
         * If the queue is full, all other connections will be refused.
         * <p>
         * Default: operating system specific.
         *
         * @param maxPending size of the queue
         * @throws IllegalArgumentException if {@code maxPending} is lower than 1.
         */
        @OptionalBuilderArgument
        Builder maxPendingConnections(int maxPending);

        /**
         * Whether multiple sockets can use the binding address.
         * If true this allows more than one socket to bind to this address.
         * <p>
         * Default: false
         */
        @OptionalBuilderArgument
        Builder reuseAddress(boolean reuseAddress);

        /**
         * Creates a new {@link ServerSocket} using the specified arguments.
         *
         * @return newly created {@link ServerSocket}.
         * @throws IllegalArgumentException if not all methods with {@link RequiredBuilderArgument} we're called.
         */
        @NotNull
        ServerSocket build() throws GeneralSecurityException;
    }
}
