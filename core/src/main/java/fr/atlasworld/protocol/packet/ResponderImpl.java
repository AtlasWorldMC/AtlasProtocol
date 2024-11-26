package fr.atlasworld.protocol.packet;

import com.google.protobuf.Message;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

// TODO
public class ResponderImpl implements Responder {
    private final Channel channel;

    public ResponderImpl(Channel channel) {
        this.channel = channel;
    }

    @Override
    public CompletableFuture<Void> acknowledge() {
        return null;
    }

    @Override
    public CompletableFuture<Void> acknowledge(@NotNull Duration timeout) {
        return null;
    }

    @Override
    public <M extends Message> CompletableFuture<Void> respond(M response, short code) {
        return null;
    }

    @Override
    public CompletableFuture<Void> respondEmpty(short code) {
        return null;
    }
}
