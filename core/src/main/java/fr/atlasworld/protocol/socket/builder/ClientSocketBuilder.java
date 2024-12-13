package fr.atlasworld.protocol.socket.builder;

import com.google.common.base.Preconditions;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.ServerInfo;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.security.HandshakeHandler;
import fr.atlasworld.protocol.socket.ClientSocket;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import fr.atlasworld.registry.Registry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static fr.atlasworld.protocol.handler.HandshakeHandler.ASYMMETRIC_KEY_ALGORITHM;

public class ClientSocketBuilder implements ClientSocket.Builder {
    private final Bootstrap bootstrap;

    private InetSocketAddress address;
    private HandshakeHandler handshakeHandler;
    private Registry<Packet> registry;
    private EventNode<Event> rootNode;

    private UUID identifier;
    private KeyPair keys;
    private boolean customAuthentication;
    private Predicate<ServerInfo> compatibilityResolver;

    private int rateLimit;
    private long requestTimeout;
    private long handshakeTimeout;

    public ClientSocketBuilder() {
        this.bootstrap = new Bootstrap();
        this.address = new InetSocketAddress(AtlasProtocol.DEFAULT_PORT);

        this.customAuthentication = false;
        this.compatibilityResolver = info -> true; // Always accept

        this.rateLimit = 50;
        this.requestTimeout = Duration.ofSeconds(30).toMillis();
        this.handshakeTimeout = Duration.ofMinutes(2).toMillis();
    }

    @Override
    public ClientSocket.Builder connectAddress(@NotNull InetSocketAddress address) {
        Preconditions.checkNotNull(address);

        this.address = address;
        return this;
    }

    @Override
    public ClientSocket.Builder rootNode(@NotNull EventNode<Event> rootNode) {
        Preconditions.checkNotNull(rootNode);

        this.rootNode = rootNode;
        return this;
    }

    @Override
    public ClientSocket.Builder handshake(@NotNull HandshakeHandler handler) {
        Preconditions.checkNotNull(handler);

        this.handshakeHandler = handler;
        return this;
    }

    @Override
    public ClientSocket.Builder registry(@NotNull Registry<Packet> registry) {
        Preconditions.checkNotNull(registry);

        this.registry = registry;
        return this;
    }

    @Override
    public ClientSocket.Builder authenticate(@NotNull UUID identifier, @NotNull PrivateKey challengeKey) {
        return this.authenticate(identifier, new KeyPair(null , challengeKey));
    }

    @Override
    public ClientSocket.Builder authenticate(@NotNull UUID identifier, @NotNull KeyPair pair) {
        Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(pair);
        Preconditions.checkNotNull(pair.getPrivate(), "Missing private key!");
        Preconditions.checkArgument(pair.getPrivate().getAlgorithm()
                .equals(ASYMMETRIC_KEY_ALGORITHM), "Only '" + ASYMMETRIC_KEY_ALGORITHM + "' are supported!");

        this.identifier = identifier;
        this.keys = pair;

        this.customAuthentication = false;

        return this;
    }

    @Override
    public ClientSocket.Builder compatibilityResolver(@NotNull Predicate<ServerInfo> resolver) {
        Preconditions.checkNotNull(resolver);

        this.compatibilityResolver = resolver;
        return this;
    }

    @Override
    public ClientSocket.Builder rateLimit(int rateLimit) {
        Preconditions.checkArgument(rateLimit > 0, "Rate limit may not be negative!");

        this.rateLimit = rateLimit;
        return this;
    }

    @Override
    public ClientSocket.Builder requestTimeout(@NotNull Duration timeout) {
        Preconditions.checkNotNull(timeout);
        Preconditions.checkArgument(timeout.toMillis() > 0, "Timeout cannot be negative!");

        this.requestTimeout = timeout.toMillis();
        return this;
    }

    @Override
    public ClientSocket.Builder connectTimeout(@NotNull Duration timeout) {
        Preconditions.checkNotNull(timeout);
        Preconditions.checkArgument(timeout.toMillis() > 0, "Timeout cannot be negative!");

        this.handshakeTimeout = timeout.toMillis();
        return this;
    }

    @Override
    public ClientSocket.Builder enableFastTCP(boolean fastTcp) {
        this.bootstrap.option(ChannelOption.TCP_FASTOPEN_CONNECT, fastTcp);
        return this;
    }

    @Override
    public ClientSocket.Builder enableNoDelay(boolean noDelay) {
        this.bootstrap.option(ChannelOption.TCP_NODELAY, noDelay);
        return this;
    }

    @Override
    public @NotNull ClientSocket build() throws GeneralSecurityException {
        Preconditions.checkNotNull(this.registry, "Missing packet registry, please provide one!");
        Preconditions.checkNotNull(this.rootNode, "Missing root node, please provide one!");
        Preconditions.checkArgument(!(this.customAuthentication && (this.identifier == null || this.keys == null)));

        return new ClientSocketImpl(this.address, this.identifier, this.keys, this.customAuthentication, this.registry,
                this.compatibilityResolver, this.rootNode, this.bootstrap, this.requestTimeout, this.handshakeTimeout,
                this.handshakeHandler, this.rateLimit);
    }
}
