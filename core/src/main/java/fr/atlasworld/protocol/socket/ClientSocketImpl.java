package fr.atlasworld.protocol.socket;

import fr.atlasworld.common.security.Encryptor;
import fr.atlasworld.common.security.encryptor.KeyPairEncryptor;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.ServerInfo;
import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.event.NetworkEvent;
import fr.atlasworld.protocol.event.socket.SocketClosedEvent;
import fr.atlasworld.protocol.event.socket.SocketOpenedEvent;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.security.HandshakeHandler;
import fr.atlasworld.protocol.socket.init.ClientSocketInitializer;
import fr.atlasworld.registry.Registry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientSocketImpl implements ClientSocket {
    private final UUID identifier;
    private final KeyPair sessionKeyPair;
    private final Encryptor sessionEncryptor;

    private final InetSocketAddress address;

    private final Registry<Packet> registry;
    private final Predicate<ServerInfo> compatibilityResolver;

    private final EventNode<Event> rootNode;
    private final EventNode<NetworkEvent> node;

    private final long defaultTimeout;
    private final long handshakeTimeout;

    private final boolean customAuth;
    private final HandshakeHandler handler;

    private final Bootstrap bootstrap;

    private EventLoopGroup workerGroup;
    private ConnectionImpl connection;

    private volatile boolean running;

    public ClientSocketImpl(InetSocketAddress address, UUID identifier, KeyPair sessionKeyPair,
                            boolean customConnection, Registry<Packet> registry,
                            Predicate<ServerInfo> compatibilityResolver, EventNode<Event> rootNode,
                            Bootstrap bootstrap, long timeout, long handshakeTimeout, HandshakeHandler handler) throws GeneralSecurityException {

        this.identifier = identifier;
        this.address = address;
        this.sessionKeyPair = sessionKeyPair;
        this.sessionEncryptor = new KeyPairEncryptor(this.sessionKeyPair);

        this.registry = registry;
        this.compatibilityResolver = compatibilityResolver;

        this.rootNode = rootNode;
        this.node = this.rootNode.createChildNode("client-socket-" + this.hashCode(), NetworkEvent.class,
                event -> event.socket() == this);

        this.defaultTimeout = timeout;
        this.handshakeTimeout = handshakeTimeout;

        this.customAuth = customConnection;
        this.handler = handler;

        this.bootstrap = bootstrap;
        this.bootstrap.channel(NioServerSocketChannel.class);
        this.bootstrap.handler(new ClientSocketInitializer(this));
    }

    @Override
    public EventNode<NetworkEvent> eventNode() {
        return this.node;
    }

    @Override
    public boolean running() {
        return this.running;
    }

    @Override
    public Side side() {
        return Side.CLIENT;
    }

    @Override
    public InetSocketAddress address() {
        return this.address;
    }

    @Override
    public CompletableFuture<Void> start() {
        if (this.running)
            throw new IllegalStateException("Socket is already running!");

        if (this.workerGroup == null || this.workerGroup.isTerminated() || this.workerGroup.isShuttingDown())
            this.workerGroup = new NioEventLoopGroup();

        ChannelFuture future = this.bootstrap
                .group(this.workerGroup)
                .bind(this.address);

        future.addListener((ChannelFutureListener) bindFuture -> {
            if (!bindFuture.isSuccess()) {
                this.cleanUp();
                return;
            }

            this.running = true;
            CompletableFuture.runAsync(() -> {
                this.rootNode.callEvent(new SocketOpenedEvent(this));
            });

            this.connection = new ConnectionImpl(future.channel(), this.identifier, this,
                    this.defaultTimeout, this.customAuth, this.rootNode);

            this.connection.channel().closeFuture().addListener(closeFuture -> {
                this.running = false;
                this.connection = null;
                this.cleanUp();

                this.rootNode.callEvent(new SocketClosedEvent(this));
            });
        });

        return ApiBridge.waitOnChannel(future);
    }

    @Override
    public CompletableFuture<Void> stop(boolean interrupt) {
        if (!this.running)
            throw new IllegalStateException("Socket is not running!");

        if (!interrupt)
            this.connection.disconnect("Client Stopping!").join();

        return ApiBridge.waitOnChannel(this.connection.channel().close());
    }

    private void cleanUp() {
        this.workerGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
    }

    public Registry<Packet> registry() {
        return this.registry;
    }

    @NotNull
    @Override
    public ConnectionImpl connection() {
        if (!this.running || this.connection == null)
            throw new IllegalStateException("Socket is not running!");

        return this.connection;
    }

    public boolean resolveCompatibility(ServerInfo serverInfo) {
        return this.compatibilityResolver.test(serverInfo);
    }

    public Encryptor clientEncryptor() {
        return this.sessionEncryptor;
    }
}
