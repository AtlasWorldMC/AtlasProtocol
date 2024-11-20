package fr.atlasworld.protocol.socket;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.ConnectionGroupImpl;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.socket.init.ServerSocketInitializer;
import fr.atlasworld.registry.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerSocketImpl implements ServerSocket {
    private final InetSocketAddress address;
    private final KeyPair sessionKeyPair;

    private final ServerBootstrap bootstrap;

    private final Registry<Packet<?>> registry;
    private final EventNode<Event> rootNode;

    private final ConnectionGroupImpl globalConnectionGroup;

    private EventLoopGroup bossGroup, workerGroup;
    private Channel serverChannel;
    private boolean running;

    private ServerSocketImpl(ServerBootstrap bootstrap, EventNode<Event> rootNode, InetSocketAddress bindAddress, KeyPair sessionKeyPair, Registry<Packet<?>> registry) {
        this.address = bindAddress;
        this.sessionKeyPair = sessionKeyPair;

        this.registry = registry;
        this.rootNode = rootNode;

        this.globalConnectionGroup = new ConnectionGroupImpl();

        this.bootstrap = bootstrap;
        this.bootstrap.channel(NioServerSocketChannel.class);
        this.bootstrap.childHandler(new ServerSocketInitializer());
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

        if (this.workerGroup == null || this.workerGroup.isTerminated() || this.bossGroup.isShuttingDown())
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

            this.serverChannel.closeFuture().addListener(closeFuture -> {

            })
        });
    }

    @Override
    public CompletableFuture<Void> stop(boolean interrupt) {

    }

    private void cleanUp() {
        this.bossGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
        this.workerGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS);
    }
}
