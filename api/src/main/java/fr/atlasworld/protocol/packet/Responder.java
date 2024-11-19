package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Responder, allows you to respond to requests.
 */
public interface Responder {

    /**
     * Acknowledge the request, but respond to it later.
     * <p>
     * Acknowledging a request still includes a time-out,
     * if you don't respond in the 10min the request will time out.
     * <p>
     * You can change this time out using {@link #acknowledge(Duration)}
     *
     * @return future of the acknowledgment.
     *
     * @throws UnsupportedOperationException if the requested was already responded to.
     */
    CompletableFuture<Void> acknowledge();

    /**
     * Acknowledge the request, but respond to it later.
     *
     * @param timeout the time before the acknowledgment request time out.
     *
     * @return future of the acknowledgment.
     *
     * @throws IllegalArgumentException if {@code timeout} exceeds 30min.
     * @throws UnsupportedOperationException if the requested was already responded to.
     */
    CompletableFuture<Void> acknowledge(@NotNull Duration timeout);

    /**
     * Respond to the request.
     *
     * @param response response to send.
     *
     * @return future of the response.
     *
     * @throws UnsupportedOperationException if the requested was already responded to.
     */
    <M extends Message> CompletableFuture<Void> respond(M response);

    /**
     * Sends an empty response of the request.
     *
     * @return future of the response.
     */
    CompletableFuture<Void> respondEmpty();
}
