package fr.atlasworld.protocol.internal;

import fr.atlasworld.protocol.socket.ClientSocket;
import fr.atlasworld.protocol.socket.ServerSocket;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ApiBridge {
    ServerSocket.Builder createServer();
    ClientSocket.Builder createClient();
}
