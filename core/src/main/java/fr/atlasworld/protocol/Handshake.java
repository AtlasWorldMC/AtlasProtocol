package fr.atlasworld.protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.UnauthorizedRequestException;
import fr.atlasworld.protocol.generated.HandshakeWrapper;
import fr.atlasworld.protocol.security.Authenticator;
import fr.atlasworld.protocol.socket.Socket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Handshake {
    private final Side side;
    private final AtomicInteger state;

    private final Authenticator authenticator;
    private final Socket socket;
    private final EventNode<Event> rootNode;

    private final long timeout;
    private final long authenticationTimeout;

    private boolean initialized;
    private boolean finished;
    private SecretKey key;

    // Identity
    private UUID identifier;
    private PublicKey publicKey;
    private boolean customAuthenticator;

    public Handshake(Authenticator authenticator, Socket socket, EventNode<Event> rootNode, long timeout, long authenticationTimeout) {
        this.state = new AtomicInteger(0);
        this.side = socket.side();

        this.authenticator = authenticator;
        this.socket = socket;
        this.rootNode = rootNode;

        this.timeout = timeout;
        this.authenticationTimeout = authenticationTimeout;
    }

    public void handle(ByteBuf packet, ChannelHandlerContext ctx) throws NetworkException {
        try {
            switch (this.state.get()) {
                case 0 -> this.initialize(packet, ctx);
            }
        } catch (Throwable e) {
            if (e instanceof NetworkException networkEx)
                throw networkEx;

            throw new UnauthorizedRequestException("Unable to complete handshake!", e, NetworkException.UNDEFINED_COMMUNICATION_IDENTIFIER);
        } finally {
            packet.release();
        }
    }

    // State 1
    private void initialize(ByteBuf packet, ChannelHandlerContext ctx) throws InvalidProtocolBufferException, NetworkException {
        HandshakeWrapper.Initialize initializePayload = HandshakeWrapper.Initialize.parseFrom(packet.nioBuffer().array());

        // Fill in identity
        this.identifier = new UUID(initializePayload.getIdMostSig(), initializePayload.getIdLeastSig());
        this.customAuthenticator = initializePayload.getCustom();

        ConnectionImpl connection = new ConnectionImpl(ctx.channel(), this.identifier, this.socket, this.timeout,
                this.customAuthenticator, this.rootNode);
        ctx.channel().attr(ApiBridge.CONNECTION_ATTR).set(connection); // Initialize the connection.

        this.initialized = true;

        if (!this.customAuthenticator) {
            this.publicKey = this.authenticator.authenticate(connection, this.identifier);
            connection.updateKey(this.publicKey);
        }
    }
}
