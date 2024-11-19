package fr.atlasworld.protocol.connection;

import com.google.protobuf.Message;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.event.ConnectionEvent;
import fr.atlasworld.protocol.packet.Response;
import fr.atlasworld.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ConnectionGroupImpl implements ConnectionGroup {
    private final Set<Connection> connections;
    private final EventNode<ConnectionEvent> node;

    public ConnectionGroupImpl() {
        this.connections = new HashSet<>();
        this.node = EventNode.create("connection-group-" + this.hashCode(), ConnectionEvent.class);
    }

    @Override
    public EventNode<ConnectionEvent> eventNode() {
        return this.node;
    }

    @Override
    public int averagePing() {
        if (this.connections.isEmpty())
            return -1;

        synchronized (this.connections) {
            int totalPing = 0;

            for (Connection connection : this.connections) {
                if (connection.ping() == -1 || !connection.connected())
                    continue;

                totalPing += connection.ping();
            }

            return totalPing / this.connections.size();
        }
    }

    @Override
    public CompletableFuture<Void> disconnect(boolean force) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        synchronized (this.connections) {
            for (Connection connection : this.connections) {
                if (!connection.connected())
                    continue;

                futures.add(connection.disconnect(force));
            }
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public <P extends Message, R extends Message> Set<CompletableFuture<Response<R>>> sendPacket(@NotNull RegistryKey key, @NotNull P payload, @NotNull Class<R> response) {
        return Set.of();
    }

    @Override
    public Set<Connection> connections() {
        return Set.of();
    }
}
