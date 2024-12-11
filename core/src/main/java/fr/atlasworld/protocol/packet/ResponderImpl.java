package fr.atlasworld.protocol.packet;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import fr.atlasworld.protocol.ApiBridge;
import fr.atlasworld.protocol.generated.AcknowledgementWrapper;
import fr.atlasworld.protocol.generated.EmptyWrapper;
import fr.atlasworld.protocol.handler.PacketPackage;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TODO
public class ResponderImpl implements Responder {
    public static final Duration DEFAULT_ACK_TIMEOUT = Duration.of(2, ChronoUnit.MINUTES);

    private final Channel channel;
    private final UUID requestIdentifier;

    public ResponderImpl(Channel channel, UUID requestIdentifier) {
        this.channel = channel;
        this.requestIdentifier = requestIdentifier;
    }

    @Override
    public CompletableFuture<Void> acknowledge() {
        return this.acknowledge(DEFAULT_ACK_TIMEOUT);
    }

    @Override
    public CompletableFuture<Void> acknowledge(@NotNull Duration timeout) {
        Preconditions.checkNotNull(timeout);

        if (!this.channel.isActive())
            return CompletableFuture.completedFuture(null);

        AcknowledgementWrapper.Acknowledge acknowledge = AcknowledgementWrapper.Acknowledge
                .newBuilder().setTimeout(timeout.toMillis()).build();
        PacketPackage packet = PacketPackage.createResponsePackage(this.requestIdentifier, (short) 0, acknowledge);

        return ApiBridge.waitOnChannel(this.channel.writeAndFlush(packet));
    }

    @Override
    public CompletableFuture<Void> respond(Message response, short code) {
        Preconditions.checkNotNull(response);
        Preconditions.checkArgument(code != 0, "Acknowledge code is not allowed, use #acknowledge().");

        if (!this.channel.isActive())
            return CompletableFuture.completedFuture(null);

        PacketPackage packet = PacketPackage.createResponsePackage(this.requestIdentifier, code, response);
        return ApiBridge.waitOnChannel(this.channel.writeAndFlush(packet));
    }

    @Override
    public CompletableFuture<Void> respondEmpty(short code) {
        return this.respond(EmptyWrapper.Empty.newBuilder().build(), code);
    }
}
