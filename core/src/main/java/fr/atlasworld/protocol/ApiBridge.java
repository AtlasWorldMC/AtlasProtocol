package fr.atlasworld.protocol;

import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.registry.Registry;
import fr.atlasworld.registry.RegistryKey;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class ApiBridge implements fr.atlasworld.protocol.internal.ApiBridge {
    public static final Logger LOGGER = LoggerFactory.getLogger(AtlasProtocol.class);

    public static final RegistryKey DISCONNECT_PACKET = new RegistryKey("system", "disconnect");

    public static final AttributeKey<ConnectionImpl> CONNECTION_ATTR = AttributeKey.newInstance("id");

    public static CompletableFuture<Void> waitOnChannel(ChannelFuture future) {
        CompletableFuture<Void> waitingFuture = new CompletableFuture<>();

        future.addListener(f -> {
            if (future.isSuccess()) {
                waitingFuture.complete(null);
                return;
            }

            if (future.isCancelled()) {
                waitingFuture.completeExceptionally(new CancellationException());
                return;
            }

            waitingFuture.completeExceptionally(f.cause());
        });

        return waitingFuture;
    }
}
