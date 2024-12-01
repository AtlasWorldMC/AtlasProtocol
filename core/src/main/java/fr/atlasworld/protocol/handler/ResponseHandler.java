package fr.atlasworld.protocol.handler;

import com.google.common.base.Preconditions;
import fr.atlasworld.protocol.packet.Response;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class ResponseHandler {
    private final CompletableFuture<Response> future;
    private final UUID identifier;

    private volatile boolean acknowledged;
    private volatile boolean completed;

    public UUID identifier() {
        return this.identifier;
    }

    public boolean acknowledged() {
        return this.acknowledged;
    }

    public ResponseHandler(@NotNull CompletableFuture<Response> future, @NotNull UUID identifier) {
        Preconditions.checkNotNull(future);
        Preconditions.checkNotNull(identifier);

        this.future = future;
        this.identifier = identifier;

        this.acknowledged = false;
        this.completed = false;
    }

    /**
     * Times out a request.
     *
     * @return true if the handler should be removed from waiting list.
     */
    public boolean timeout() {
        if (this.acknowledged || this.completed)
            return false; // If the request has been acknowledged we do not remove the handler yet.

        if (!this.future.isDone())
            this.future.completeExceptionally(new TimeoutException("Awaiting response timed-out."));

        return true;
    }

    public void acknowledge() {
        if (this.acknowledged)
            throw new IllegalStateException("Response has already been acknowledged!");

        if (this.completed)
            return;

        this.acknowledged = true;
    }

    /**
     * Times out acknowledgement.
     */
    public void timeoutAcknowledgement() {
        if (!this.acknowledged)
            throw new IllegalArgumentException("The request was never acknowledged!");

        if (!this.future.isDone())
            this.future.completeExceptionally(new TimeoutException("Awaiting acknowledged response timed-out."));

    }

    public void respond(Response response) {
        if (this.completed)
            throw new IllegalStateException("Handler was already responded to!");

        this.completed = true;

        if (!this.future.isDone())
            this.future.complete(response);
    }
}
