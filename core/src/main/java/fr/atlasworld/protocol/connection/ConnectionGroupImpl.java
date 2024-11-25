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
import java.util.stream.Collectors;

public class ConnectionGroupImpl implements ConnectionGroup {
    private final Map<UUID, ConnectionImpl> connections;
    private final EventNode<ConnectionEvent> node;

    private final ReadWriteLock lock;

    public ConnectionGroupImpl() {
        this.connections = new HashMap<>();
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

        this.lock.readLock().lock();
        try {
            for (Connection connection : this.connections.values()) {
                if (!connection.connected() || connection.ping() == -1)
                    continue;

                totalPing += connection.ping();
                connectionCount++;
            }
        } finally {
            this.lock.readLock().unlock();
        }

        return connectionCount > 0 ? (totalPing / connectionCount) : -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Void> disconnect(boolean force) {
        CompletableFuture<Void>[] futures;

        this.lock.readLock().lock();
        try {
            futures = this.connections.values().stream()
                    .filter(ConnectionImpl::connected)
                    .map(connection -> connection.disconnect(force))
                    .toArray(CompletableFuture[]::new);
        } finally {
            this.lock.readLock().unlock();
        }

        if (futures.length == 0)
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(futures);
    }

    @Override
    public <P extends Message> Set<CompletableFuture<Response>> sendPacket(@NotNull RegistryKey key, @NotNull P payload) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(payload);

        this.lock.readLock().lock();
        try {
            return this.connections.values().stream()
                    .filter(ConnectionImpl::connected)
                    .map(connection -> connection.sendPacket(key, payload))
                    .collect(Collectors.toUnmodifiableSet());
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Set<Connection> connections() {
        this.lock.readLock().lock();
        try {
            return Collections.unmodifiableSet(this.connections);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Connection> retrieveConnection(@NotNull UUID identifier) {
        this.lock.readLock().lock();
        try {
            return Optional.ofNullable(this.connections.get(identifier));
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void registerConnection(@NotNull ConnectionImpl connection) {
        Preconditions.checkNotNull(connection);
        Preconditions.checkArgument(connection.connected(), "Connection is no longer connected!");

        this.lock.writeLock().lock();
        try {
            if (this.connections.containsKey(connection.identifier()))
                throw new IllegalArgumentException("Connection already present!");

            this.connections.put(connection.identifier(), connection);
        } finally {
            this.lock.writeLock().unlock();
        }

        connection.eventNode().addChildNode(this.node);
    }

    public boolean unregisterConnection(@NotNull ConnectionImpl connection) {
        Preconditions.checkNotNull(connection);

        this.lock.writeLock().lock();
        try {
            if (this.connections.remove(connection.identifier()) != null) {
                connection.eventNode().removeChildNode(this.node.name());
                return true;
            }

            return false;
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
