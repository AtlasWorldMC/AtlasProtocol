package fr.atlasworld.protocol.connection;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.generated.EmptyWrapper;
import fr.atlasworld.protocol.handler.PacketPackage;
import fr.atlasworld.protocol.handler.ResponseHandler;
import fr.atlasworld.protocol.packet.ResponderImpl;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.protocol.socket.Socket;
import fr.atlasworld.registry.RegistryKey;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionImpl implements Connection {
    private static final String NODE_NAME = "connection-%s-%s";
    private static final HashedWheelTimer TIMER = new HashedWheelTimer();

    private final Map<UUID, ResponseHandler> awaitingResponses;

    private final Channel channel;
    private final UUID identifier;
    private final PublicKey key;
    private final AtomicLong timeout;

    private final Socket socket;

    private volatile int ping;
    private final EventNode<ConnectionEvent> node;

    public ConnectionImpl(Channel channel, UUID identifier, PublicKey key, Socket socket, long timeout) {
        this.awaitingResponses = new ConcurrentHashMap<>();

        this.channel = channel;
        this.identifier = identifier;
        this.key = key;
        this.timeout = new AtomicLong(timeout);
        this.socket = socket;

        this.ping = -1;
        this.node = socket.eventNode()
                .createChildNode(String.format(NODE_NAME, this.channel.remoteAddress(), this.hashCode()));
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

    public synchronized void updatePing(int ping) {
        this.ping = Math.max(ping, 0);
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
    public <P extends Message> @NotNull CompletableFuture<Response> sendPacket(@NotNull RegistryKey key, @Nullable P payload) {
        Preconditions.checkNotNull(key);

        if (!this.channel.isActive())
            throw new IllegalStateException("Connection Disconnected!");

        long currentTimeout = this.timeout.get(); // Makes sure the sent packet and the scheduler have the same timeout.
        PacketPackage packet = PacketPackage.createRequestPackage(currentTimeout, key,
                payload == null ? EmptyWrapper.Empty.newBuilder().build() : payload); // Allows to send requests without payload

        CompletableFuture<Response> future = new CompletableFuture<>();
        this.channel.writeAndFlush(packet).addListener(writeFuture -> {
            if (!writeFuture.isSuccess())
                future.completeExceptionally(writeFuture.cause());

            this.scheduleResponse(future, packet.requestId(), currentTimeout);
        });

        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect(boolean force) {
        if (!this.channel.isActive())
            throw new IllegalStateException("Connection Disconnected!");

        this.sendPacket(ApiBridge.DISCONNECT_PACKET, EmptyWrapper.Empty.newBuilder().build());
    }

    @Override
    public @NotNull Duration timeout() {
        return Duration.of(this.timeout.get(), ChronoUnit.MILLIS);
    }

    @Override
    public void timeout(@NotNull Duration timeout) {
        Preconditions.checkNotNull(timeout);
        Preconditions.checkArgument(timeout.isPositive() && !timeout.isZero(), "Timeout must be higher than 0!");

        this.timeout.set(timeout.get(ChronoUnit.MILLIS));
    }

    @Override
    public @NotNull Socket socket() {
        return this.socket;
    }

    @ApiStatus.Internal
    public Channel channel() {
        return this.channel;
    }

    public ResponderImpl createResponder(UUID requestIdentifier) {
        return new ResponderImpl(this.channel, requestIdentifier);
    }

    public void acknowledgeRequest(UUID identifier, long time) {
        ResponseHandler handler = this.awaitingResponses.get(identifier);
        handler.acknowledge();

        TIMER.newTimeout(unused -> {
            handler.timeoutAcknowledgement();
            this.awaitingResponses.remove(identifier);
        }, time, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConnectionImpl other))
            return false;

        return this.channel.equals(other.channel);
    }

    private void scheduleResponse(CompletableFuture<Response> future, UUID identifier, long timeout) {
        if (this.awaitingResponses.containsKey(identifier))
            throw new IllegalArgumentException("There is an already existing request with this identifier: %s" + identifier);

        ResponseHandler handler = new ResponseHandler(future, identifier);
        this.awaitingResponses.put(identifier, handler);

        TIMER.newTimeout(unused -> {
            if (!this.channel.isActive())
                return;

            if (handler.timeout())
                this.awaitingResponses.remove(identifier);
        }, timeout, TimeUnit.MILLISECONDS);
    }
}
