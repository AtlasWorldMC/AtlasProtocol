package fr.atlasworld.protocol.socket;

import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.Connection;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Generic Socket Interface, implements the base sockets functions.
 */
public interface Socket {

    /**
     * Checks whether the socket is running.
     * <p>
     * On client-side a running socket doesn't mean it's connected,
     * for that use {@link Connection#connected() ClientSocket#connected()}.
     *
     * @return true if the socket is running, false otherwise.
     */
    boolean running();

    /**
     * Retrieve the side of the socket.
     *
     * @return side of the socket.
     */
    Side side();

    /**
     * Address on which the socket is connected / bound.
     *
     * @return socket address.
     */
    InetSocketAddress address();

    /**
     * Starts the socket.
     *
     * @return future of the starting process.
     *
     * @throws IllegalStateException if the socket is already running.
     */
    CompletableFuture<Void> start();

    /**
     * Stops the socket.
     *
     * @param interrupt whether to interrupt the socket,
     *                  this will make it stop instantly but won't gracefully terminate connections.
     *
     * @return future of the stopping process.
     *
     * @throws IllegalStateException if the socket is not running.
     */
    CompletableFuture<Void> stop(boolean interrupt);
}
