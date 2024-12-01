package fr.atlasworld.protocol.socket;

import com.google.protobuf.Message;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.protocol.socket.init.ClientSocketInitializer;
import fr.atlasworld.protocol.socket.init.ServerSocketInitializer;
import fr.atlasworld.registry.Registry;
import fr.atlasworld.registry.RegistryKey;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClientSocketImpl implements ClientSocket {
    private final UUID identifier;
    private final KeyPair sessionKeyPair;

    private final InetSocketAddress address;

    private final Registry<Packet> registry;
    private final EventNode<Event> rootNode;
    private final EventNode<ConnectionEvent> node;

    private final long defaultTimeout;

    private final Bootstrap bootstrap;

    private EventLoopGroup workerGroup;
    private ConnectionImpl connection;

    private volatile boolean running;

    public ClientSocketImpl(InetSocketAddress address, UUID identifier, KeyPair sessionKeyPair, Registry<Packet> registry, EventNode<Event> rootNode, Bootstrap bootstrap, long timeout) {
        this.identifier = identifier;
        this.address = address;
        this.sessionKeyPair = sessionKeyPair;

        this.registry = registry;
        this.rootNode = rootNode;
        this.node = this.rootNode.createChildNode("client-socket-" + this.hashCode(), ConnectionEvent.class,
                event -> event.connection().socket() == this);

        this.defaultTimeout = timeout;

        this.bootstrap = bootstrap;
        this.bootstrap.channel(NioServerSocketChannel.class);
        this.bootstrap.handler(new ClientSocketInitializer(this));
    }

    @Override
    public EventNode<ConnectionEvent> eventNode() {
        return this.node;
    }

    @Override
    public @NotNull UUID identifier() {
        return this.identifier;
    }

    @Override
    public @NotNull PublicKey publicKey() {
        return this.sessionKeyPair.getPublic();
    }

    @Override
    public int ping() {
        return this.connection == null ? -1 : this.connection.ping();
    }

    @Override
    public boolean connected() {
        return this.connection == null || this.connection.connected();
    }

    @Override
    public @NotNull InetSocketAddress remoteAddress() {
        return this.address;
    }

    @Override
    public @NotNull <P extends Message> CompletableFuture<Response> sendPacket(@NotNull RegistryKey key, @NotNull P payload) {
        if (this.connection == null)
            throw new IllegalArgumentException("Connection Disconnected!");

        return this.connection.sendPacket(key, payload);
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect(boolean force) {
        if (this.connection == null)
            throw new IllegalArgumentException("Connection Disconnected!");

        return this.connection.disconnect(force);
    }

    @Override
    public @NotNull Duration timeout() {
        if (this.connection == null)
            return Duration.of(this.defaultTimeout, ChronoUnit.MILLIS);

        return this.connection.timeout();
    }

    @Override
    public void timeout(@NotNull Duration timeout) {
        if (this.connection == null)
            return;

        this.connection.timeout(timeout);
    }

    @Override
    public @NotNull Socket socket() {
        return this;
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

            this.connection = new ConnectionImpl(future.channel(), this.identifier, this.sessionKeyPair.getPublic(),
                    this, this.defaultTimeout);
            this.running = true;

            this.connection.channel().closeFuture().addListener(closeFuture -> {
                this.running = false;
                this.connection = null;
                this.cleanUp();
            });
        });

        return ApiBridge.waitOnChannel(future);
    }

    @Override
    public CompletableFuture<Void> stop(boolean interrupt) {
        if (!this.running)
            throw new IllegalStateException("Socket is not running!");

        if (!interrupt)
            this.connection.disconnect(true).join();

        return ApiBridge.waitOnChannel(this.connection.channel().close());
    }

    private void cleanUp() {
        this.workerGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
    }

    public Registry<Packet> registry() {
        return this.registry;
    }

    public ConnectionImpl connection() {
        return this.connection;
    }
}
