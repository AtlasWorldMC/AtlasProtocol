package fr.atlasworld.protocol.connection;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class ConnectionGroupImpl implements ConnectionGroup {
    private final Set<ConnectionImpl> connections;
    private final EventNode<ConnectionEvent> node;

    private final ReadWriteLock lock;

    public ConnectionGroupImpl() {
        this.connections = new HashSet<>();
        this.node = EventNode.create("connection-group-" + this.hashCode(), ConnectionEvent.class);
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public EventNode<ConnectionEvent> eventNode() {
        return this.node;
    }

    @Override
    public int averagePing() {
        int connectionCount = 0;
        int totalPing = 0;

        for (Connection connection : this.connections) {
            if (!connection.connected() || connection.ping() == -1)
                continue;

            totalPing += connection.ping();
            connectionCount++;
        }

        this.lock.readLock().unlock();

        if (connectionCount < 1) {
            return -1;
        }

        return totalPing / connectionCount;
    }

    @Override
    public CompletableFuture<Void> disconnect(boolean force) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        this.lock.readLock().lock();
        for (Connection connection : this.connections) {
            if (!connection.connected())
                continue;

            futures.add(connection.disconnect(force));
        }
        this.lock.readLock().unlock();

        if (futures.isEmpty())
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public <P extends Message, R extends Message> Set<CompletableFuture<Response<R>>> sendPacket(@NotNull RegistryKey key, @NotNull P payload, @NotNull Class<R> response) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(payload);
        Preconditions.checkNotNull(response);

        Set<CompletableFuture<Response<R>>> futures = new HashSet<>();

        this.lock.readLock().lock();
        for (Connection connection : this.connections) {
            if (!connection.connected())
                continue;

            futures.add(connection.sendPacket(key, payload, response));
        }
        this.lock.readLock().unlock();

        if (futures.isEmpty())
            return Set.of();

        return futures;
    }

    @Override
    public Set<Connection> connections() {
        Set<Connection> immutableConnections;

        this.lock.readLock().lock();
        immutableConnections = Set.copyOf(this.connections);
        this.lock.readLock().unlock();

        return immutableConnections;
    }

    public void registerConnection(@NotNull ConnectionImpl connection) {
        Preconditions.checkNotNull(connection);

        this.lock.writeLock().lock();

        if (this.connections.contains(connection)) {
            this.lock.writeLock().unlock();
            throw new IllegalArgumentException("Connection already present!");
        }

        this.connections.add(connection);
        this.lock.writeLock().unlock();

        return;
    }

    public boolean unregisterConnection(@NotNull ConnectionImpl connection) {
        Preconditions.checkNotNull(connection);

        this.lock.writeLock().lock();
        boolean removed = this.connections.remove(connection);
        this.lock.writeLock().unlock();

        return removed;
    }
}
