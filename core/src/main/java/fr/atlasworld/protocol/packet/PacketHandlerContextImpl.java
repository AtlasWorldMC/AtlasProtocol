package fr.atlasworld.protocol.packet;

import fr.atlasworld.protocol.Side;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.connection.ConnectionImpl;
import fr.atlasworld.protocol.socket.ClientSocketImpl;
import fr.atlasworld.protocol.socket.Socket;
import org.jetbrains.annotations.NotNull;

public class PacketHandlerContextImpl implements PacketHandlerContext {
    private final Connection connection;
    private final ResponderImpl responder;
    private final Socket socket;

    public PacketHandlerContextImpl(Connection connection, Socket socket) {
        this.connection = connection;
        this.socket = socket;

        this.responder = connection instanceof ConnectionImpl ?
                ((ConnectionImpl) connection).createResponder() :
                ((ClientSocketImpl) connection).connection().createResponder();
    }

    @Override
    public Responder responder() {
        return this.responder;
    }

    @Override
    public @NotNull Side side() {
        return this.socket.side();
    }

    @Override
    public @NotNull Connection source() {
        return this.connection;
    }

    @Override
    public @NotNull Socket socket() {
        return this.socket;
    }
}
