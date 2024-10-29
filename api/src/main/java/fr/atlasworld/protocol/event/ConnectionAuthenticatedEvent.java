package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.UUID;

/**
 * Event called when a connection is authenticated.
 * <p>
 * If the connection has been established securely,
 * this event is called at the same time as {@link ConnectionEstablishedEvent}.
 */
public class ConnectionAuthenticatedEvent extends ConnectionEvent {
    private final PublicKey publicKey;

    public ConnectionAuthenticatedEvent(@NotNull Connection connection, @Nullable PublicKey key) {
        super(connection);

        Preconditions.checkNotNull(key);

        this.publicKey = key;
    }

    /**
     * Retrieve the identifier of the authenticated connection.
     *
     * @return identifier of the connection.
     */
    public UUID identifier() {
        return this.connection().identifier();
    }

    /**
     * Retrieve the public key of the authenticated connection.
     *
     * @return public key of the authenticated connection.
     */
    public PublicKey key() {
        return this.publicKey;
    }
}
