package fr.atlasworld.protocol.connection;

import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.packet.ResponderImpl;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.protocol.socket.Socket;
import fr.atlasworld.registry.RegistryKey;
import io.netty.channel.Channel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionImpl implements Connection {
    private static final String NODE_NAME = "connection-%s-%s";

    private final Channel channel;
    private final UUID identifier;
    private final PublicKey key;

    private final Socket socket;

    private volatile int ping;
    private final EventNode<ConnectionEvent> node;

    public ConnectionImpl(Channel channel, UUID identifier, PublicKey key, Socket socket) {
        this.channel = channel;
        this.identifier = identifier;
        this.key = key;
        this.socket = socket;

        this.ping = -1;
        this.node = EventNode.create(String.format(NODE_NAME, channel.remoteAddress(), channel.hashCode()),
                ConnectionEvent.class, event -> event.connection().equals(this));
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
        return this.key;
    }

    @Override
    public int ping() {
        return this.ping;
    }

    @Override
    public boolean connected() {
        return this.channel.isActive();
    }

    @Override
    public @NotNull InetSocketAddress remoteAddress() {
        return (InetSocketAddress) this.channel.remoteAddress();
    }

    @Override
    public <P extends Message> @NotNull CompletableFuture<Response> sendPacket(@NotNull RegistryKey key, @NotNull P payload) {
        if (!this.channel.isActive())
            throw new IllegalStateException("Connection Disconnected!");


    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect(boolean force) {
        if (!this.channel.isActive())
            throw new IllegalStateException("Connection Disconnected!");


    }

    @Override
    public @NotNull Socket socket() {
        return this.socket;
    }

    @ApiStatus.Internal
    public Channel channel() {
        return this.channel;
    }

    public ResponderImpl createResponder() {
        return new ResponderImpl(this.channel);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConnectionImpl other))
            return false;

        return this.channel.equals(other.channel);
    }
}
