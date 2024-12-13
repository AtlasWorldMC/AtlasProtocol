package fr.atlasworld.protocol.socket;

import fr.atlasworld.common.annotation.OptionalBuilderArgument;
import fr.atlasworld.common.annotation.RequiredBuilderArgument;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.ServerInfo;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.exception.NetworkRateLimitedException;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.security.HandshakeHandler;
import fr.atlasworld.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Client-Side socket, handles all the logic of the protocol.
 */
public interface ClientSocket extends Socket {

    /**
     * Retrieve the client connection.
     *
     * @return client connection.
     *
     * @throws IllegalStateException if the socket is not running.
     */
    @NotNull
    Connection connection();

    /**
     * Builder to create a new {@link ClientSocket}.
     */
    interface Builder {

        /**
         * Create a new {@link ClientSocket.Builder}.
         *
         * @return newly created {@link ClientSocket.Builder}.
         */
        static Builder create() {
            return AtlasProtocol.createClient();
        }

        /**
         * Sets the server address to connect.
         *
         * @param address address of the server.
         */
        @RequiredBuilderArgument
        Builder connectAddress(@NotNull InetSocketAddress address);

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
         * Sets the packet registry.
         *
         * @param registry registry containing the packets.
         */
        @RequiredBuilderArgument
        Builder registry(@NotNull Registry<Packet> registry);

        /**
         * Sets the client authentication parameters required to authenticate to the server.
         * <p>
         * This uses the default authentication mechanism from the protocol.
         *
         * @param identifier   identifier of the client.
         * @param challengeKey private key used in the challenge.
         */
        @RequiredBuilderArgument
        Builder authenticate(@NotNull UUID identifier, @NotNull PrivateKey challengeKey);

        /**
         * Sets the client authentication parameters required to authenticate to the server.
         * <p>
         * This uses the default authentication mechanism from the protocol.
         *
         * @param identifier identifier of the client.
         * @param pair key pair used in the authentication challenge.
         */
        @RequiredBuilderArgument
        Builder authenticate(@NotNull UUID identifier, @NotNull KeyPair pair);

        /**
         * Sets the handshake handler.
         * <p>
         * This uses a custom user-created authentication system.
         * <br>
         * The recommended and default authentication can be done with {@link #authenticate(UUID, PrivateKey)}.
         *
         * @param handler handler to handle the handshake.
         */
        @RequiredBuilderArgument
        Builder handshake(@NotNull HandshakeHandler handler);

        /**
         * Sets the compatibility resolver of the client.
         * <p>
         * If none is defined, we simply check if our protocol version matches with the server's protocol version.
         *
         * @param resolver resolver
         */
        @OptionalBuilderArgument
        Builder compatibilityResolver(@NotNull Predicate<ServerInfo> resolver);

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
         * Whether fast TCP should be enabled.
         * <p>
         * Default: false
         */
        @OptionalBuilderArgument
        Builder enableFastTCP(boolean fastTcp);

        /**
         * Whether to disable Nagle's Algorithm.
         * This can lower the latency but at cost of bandwidth.
         * <p>
         * Default: false
         */
        @OptionalBuilderArgument
        Builder enableNoDelay(boolean noDelay);

        /**
         * Creates a new {@link ClientSocket} using the specified arguments.
         *
         * @return newly created {@link ClientSocket}.
         * @throws IllegalArgumentException if not all methods with {@link RequiredBuilderArgument} we're called.
         */
        @NotNull
        ClientSocket build() throws GeneralSecurityException;
    }
}
