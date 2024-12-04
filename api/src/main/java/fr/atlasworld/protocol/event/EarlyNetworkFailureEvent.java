package fr.atlasworld.protocol.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * Only happens on server sockets, this is called when a connection failed before,
 * it could exchange information.
 * <p>
 * This may indicate that a connection was attempted with the wrong protocol.
 */
public class EarlyNetworkFailureEvent extends NetworkEvent {
    private final InetSocketAddress remoteAddress;

    public EarlyNetworkFailureEvent(@NotNull Socket socket, @NotNull InetSocketAddress remoteAddress) {
        super(socket);

        Preconditions.checkNotNull(remoteAddress);
        this.remoteAddress = remoteAddress;
    }

    public final InetSocketAddress remoteAddress() {
        return this.remoteAddress;
    }
}
