package fr.atlasworld.protocol.socket.builder;

import com.google.common.base.Preconditions;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.security.Authenticator;
import fr.atlasworld.protocol.security.HandshakeHandler;
import fr.atlasworld.protocol.socket.ServerSocket;
import fr.atlasworld.protocol.socket.ServerSocketImpl;
import fr.atlasworld.registry.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static fr.atlasworld.protocol.handler.HandshakeHandler.ASYMMETRIC_KEY_ALGORITHM;

public class ServerSocketBuilder implements ServerSocket.Builder {
    private final Map<String, String> properties;
    private final ServerBootstrap bootstrap;

    private InetSocketAddress address;
    private Authenticator authenticator;
    private HandshakeHandler handshakeHandler;
    private Registry<Packet> registry;
    private EventNode<Event> rootNode;

    private long requestTimeout;
    private long handshakeTimeout;

    private KeyPair keyPair;

    public ServerSocketBuilder() {
        this.properties = new HashMap<>();
        this.bootstrap = new ServerBootstrap();

        this.address = new InetSocketAddress(AtlasProtocol.DEFAULT_PORT);
        this.requestTimeout = Duration.ofSeconds(30).toMillis();
        this.handshakeTimeout = Duration.ofMinutes(2).toMillis();

        this.bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            this.keyPair = KeyPairGenerator.getInstance(ASYMMETRIC_KEY_ALGORITHM).generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            ApiBridge.LOGGER.error("'" + ASYMMETRIC_KEY_ALGORITHM + "' is not supported on this system.");
            this.keyPair = null;
        }
    }

    @Override
    public ServerSocket.Builder bindAddress(@NotNull InetSocketAddress address) {
        Preconditions.checkNotNull(address);

        this.address = address;
        return this;
    }

    @Override
    public ServerSocket.Builder authenticator(@NotNull Authenticator authenticator) {
        Preconditions.checkNotNull(authenticator);

        this.authenticator = authenticator;
        return this;
    }

    @Override
    public ServerSocket.Builder rootNode(@NotNull EventNode<Event> rootNode) {
        Preconditions.checkNotNull(rootNode);

        this.rootNode = rootNode;
        return this;
    }

    @Override
    public ServerSocket.Builder handleHandshake(@NotNull HandshakeHandler handler) {
        Preconditions.checkNotNull(handler);

        this.handshakeHandler = handler;
        return this;
    }

    @Override
    public ServerSocket.Builder registry(@NotNull Registry<Packet> registry) {
        Preconditions.checkNotNull(registry);

        this.registry = registry;
        return this;
    }

    @Override
    public ServerSocket.Builder handshakeProperties(@NotNull String key, @NotNull String value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);

        this.properties.put(key, value);
        return this;
    }

    @Override
    public ServerSocket.Builder requestTimeout(@NotNull Duration timeout) {
        Preconditions.checkNotNull(timeout);
        Preconditions.checkArgument(timeout.toMillis() > 0, "Timeout cannot be negative!");

        this.requestTimeout = timeout.toMillis();
        return this;
    }

    @Override
    public ServerSocket.Builder connectTimeout(@NotNull Duration timeout) {
        Preconditions.checkNotNull(timeout);
        Preconditions.checkArgument(timeout.toMillis() > 0, "Timeout cannot be negative!");

        this.handshakeTimeout = timeout.toMillis();
        return this;
    }

    @Override
    public ServerSocket.Builder keyPair(@NotNull KeyPair pair) {
        Preconditions.checkNotNull(pair);
        Preconditions.checkNotNull(pair.getPublic());
        Preconditions.checkNotNull(pair.getPrivate());
        Preconditions.checkArgument(pair.getPublic().getAlgorithm()
                .equals(ASYMMETRIC_KEY_ALGORITHM), "Only '" + ASYMMETRIC_KEY_ALGORITHM + "' are supported!");
        Preconditions.checkArgument(pair.getPrivate().getAlgorithm()
                .equals(ASYMMETRIC_KEY_ALGORITHM), "Only '" + ASYMMETRIC_KEY_ALGORITHM + "' are supported!");

        this.keyPair = pair;
        return this;
    }

    @Override
    public ServerSocket.Builder keepAlive(boolean keepAlive) {
        this.bootstrap.childOption(ChannelOption.SO_KEEPALIVE, keepAlive);
        return this;
    }

    @Override
    public ServerSocket.Builder enableNoDelay(boolean noDelay) {
        this.bootstrap.childOption(ChannelOption.TCP_NODELAY, noDelay);
        return this;
    }

    @Override
    public ServerSocket.Builder maxPendingConnections(int maxPending) {
        this.bootstrap.option(ChannelOption.SO_BACKLOG, maxPending);
        return this;
    }

    @Override
    public ServerSocket.Builder reuseAddress(boolean reuseAddress) {
        this.bootstrap.option(ChannelOption.SO_REUSEADDR, reuseAddress);
        return this;
    }

    @Override
    public @NotNull ServerSocket build() throws GeneralSecurityException {
        Preconditions.checkNotNull(this.keyPair, "Failed to generate KeyPair, please provide one!");
        Preconditions.checkNotNull(this.rootNode, "Missing root node, please provide one!");
        Preconditions.checkArgument(!(this.authenticator == null && this.handshakeHandler == null) , "Missing authenticator or handshake handler, please provide one!");
        Preconditions.checkNotNull(this.registry, "Missing packet registry, please provide one!");

        return new ServerSocketImpl(this.bootstrap, this.rootNode, this.address, this.keyPair, this.registry,
                this.requestTimeout, this.handshakeTimeout, this.authenticator, this.handshakeHandler, this.properties);
    }
}
