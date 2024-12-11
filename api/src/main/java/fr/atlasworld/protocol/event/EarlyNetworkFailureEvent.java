package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * Called when a connection failed before, it could exchange information.
 * <p>
 * This may indicate that a connection was attempted with the wrong protocol.
 */
public class EarlyNetworkFailureEvent extends NetworkEvent {
    private final InetSocketAddress remoteAddress;
    private final Throwable cause;

    public EarlyNetworkFailureEvent(@NotNull Socket socket, @NotNull InetSocketAddress remoteAddress, Throwable cause) {
        super(socket);

        Preconditions.checkNotNull(remoteAddress);
        Preconditions.checkNotNull(cause);

        this.remoteAddress = remoteAddress;
        this.cause = cause;
    }

    /**
     * Retrieve the remote address of the attempted connection.
     *
     * @return socket address.
     */
    public final InetSocketAddress remoteAddress() {
        return this.remoteAddress;
    }

    /**
     * Retrieve the cause of the failure.
     *
     * @return
     */
    public final Throwable cause() {
        return this.cause;
    }
}
