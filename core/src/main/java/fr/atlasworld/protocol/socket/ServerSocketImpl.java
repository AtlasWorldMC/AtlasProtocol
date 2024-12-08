package fr.atlasworld.protocol.socket;

import fr.atlasworld.common.security.Encryptor;
import fr.atlasworld.common.security.encryptor.KeyPairEncryptor;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.ConnectionGroupImpl;
import fr.atlasworld.protocol.event.NetworkEvent;
import fr.atlasworld.protocol.event.socket.SocketClosedEvent;
import fr.atlasworld.protocol.event.socket.SocketOpenedEvent;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.security.Authenticator;
import fr.atlasworld.protocol.security.HandshakeHandler;
import fr.atlasworld.protocol.socket.init.ServerSocketInitializer;
import fr.atlasworld.registry.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerSocketImpl implements ServerSocket {
    private final InetSocketAddress address;
    private final KeyPair sessionKeyPair;
    private final Encryptor sessionEncryptor;

    private final ServerBootstrap bootstrap;

    private final Registry<Packet> registry;
    private final EventNode<Event> rootNode;
    private final EventNode<NetworkEvent> node;

    private final long defaultTimeout;
    private final long handshakeTimeout;

    // Security
    private final Authenticator authenticator;
    private final HandshakeHandler handler;

    private final ConnectionGroupImpl globalConnectionGroup;

    private EventLoopGroup bossGroup, workerGroup;
    private Channel serverChannel;
    private volatile boolean running;

    public ServerSocketImpl(ServerBootstrap bootstrap, EventNode<Event> rootNode, InetSocketAddress bindAddress,
                             KeyPair sessionKeyPair, Registry<Packet> registry, long defaultTimeout, long handshakeTimeout,
                             Authenticator authenticator, HandshakeHandler handler, Map<String, String> properties) throws GeneralSecurityException {
        this.address = bindAddress;
        this.sessionKeyPair = sessionKeyPair;
        this.sessionEncryptor = new KeyPairEncryptor(this.sessionKeyPair);

        this.registry = registry;
        this.handshakeTimeout = handshakeTimeout;
        this.defaultTimeout = defaultTimeout;

        this.authenticator = authenticator;
        this.handler = handler;
        this.rootNode = rootNode;
        this.node = this.rootNode.createChildNode("server-socket-" + this.hashCode(), NetworkEvent.class,
                event -> event.socket() == this);

        this.globalConnectionGroup = new ConnectionGroupImpl();

        this.bootstrap = bootstrap;
        this.bootstrap.channel(NioServerSocketChannel.class);
        this.bootstrap.childHandler(new ServerSocketInitializer(this, properties));
    }

    @Override
    public @NotNull ConnectionGroupImpl connections() {
        return this.globalConnectionGroup;
    }

    @Override
    public @NotNull KeyPair sessionKeyPair() {
        return this.sessionKeyPair;
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
        return Side.SERVER;
    }

    @Override
    public InetSocketAddress address() {
        return this.address;
    }

    @Override
    public CompletableFuture<Void> start() {
        if (this.running)
            throw new IllegalStateException("Socket is already running!");

        if (this.bossGroup == null || this.bossGroup.isTerminated() || this.bossGroup.isShuttingDown())
            this.bossGroup = new NioEventLoopGroup();

        if (this.workerGroup == null || this.workerGroup.isTerminated() || this.workerGroup.isShuttingDown())
            this.workerGroup = new NioEventLoopGroup();

        ChannelFuture future = this.bootstrap
                .group(this.bossGroup, this.workerGroup)
                .bind(this.address);

        future.addListener((ChannelFutureListener) bindFuture -> {
            if (!bindFuture.isSuccess()) {
                this.cleanUp();
                return;
            }

            this.serverChannel = bindFuture.channel();
            this.running = true;

            CompletableFuture.runAsync(() -> {
                this.rootNode.callEvent(new SocketOpenedEvent(this));
            });

            this.serverChannel.closeFuture().addListener(closeFuture -> {
                this.running = false;
                this.serverChannel = null;

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
            this.globalConnectionGroup.disconnect("Server Stopping!").join();

        return ApiBridge.waitOnChannel(this.serverChannel.close());
    }

    private void cleanUp() {
        this.bossGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
        this.workerGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
    }

    public long defaultTimeout() {
        return this.defaultTimeout;
    }

    public long handshakeTimeout() {
        return this.handshakeTimeout;
    }

    public Authenticator authenticator() {
        return this.authenticator;
    }

    public HandshakeHandler handshakeHandler() {
        return this.handler;
    }

    public Encryptor serverEncryptor() {
        return this.sessionEncryptor;
    }

    public Registry<Packet> registry() {
        return this.registry;
    }

    public ConnectionGroupImpl globalConnectionGroup() {
        return this.globalConnectionGroup;
    }

    @ApiStatus.Internal
    public EventNode<Event> rootNode() {
        return this.rootNode;
    }
}
