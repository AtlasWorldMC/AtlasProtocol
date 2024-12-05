package fr.atlasworld.protocol.connection;

import fr.atlasworld.protocol.event.connection.ConnectionRefusedEvent;
import fr.atlasworld.protocol.security.Authenticator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Sub-interface of a connection that is yet in its handshake phase,
 * in this phase you have much more power on the connection.
 * <p>
 * But you cannot send packets nor disconnect the connection without explicitly refusing its connection.
 * <p>
 * This object should be kept in the {@link Authenticator#authenticate(InsecureConnection, UUID)} method,
 * as this object is not thread-safe and should not get called on multiple threads.
 */
@NotThreadSafe
public interface InsecureConnection {

    /**
     * Unique identifier of the connection.
     * <p>
     * The identifier may be {@code null} if the connection is connecting using a custom authentication system,
     * or that it has not yet sent the appropriate information.
     *
     * @return unique identifier of the connection.
     */
    @UnknownNullability
    UUID identifier();

    /**
     * Retrieve the remote address.
     *
     * @return remote address of the connection.
     */
    @NotNull
    InetSocketAddress remoteAddress();

    /**
     * Refuses the connection,
     * send a refused connection request and forcefully ends the connection.
     *
     * @param reason reason to the denied access.
     *
     * @return future of the disconnection.
     *
     * @throws IllegalStateException if the connection is already marked as authenticated,
     *                               or that the connection has already been refused.
     */
    @NotNull
    CompletableFuture<Void> refuseConnection(@NotNull ConnectionRefusedEvent.Cause reason);

    /**
     * Authenticates the connections, this will mark the connection as trusted.
     *
     * @throws IllegalStateException if the connection is already marked as authenticated,
     *                               or that the connection has already been refused.
     * @throws UnsupportedOperationException if the authentication is managed internally,
     *                                       and does not allow to authenticate a method manually.
     */
    void authenticate();
}
