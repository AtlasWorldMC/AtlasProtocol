package fr.atlasworld.protocol.internal;

import fr.atlasworld.protocol.socket.ClientSocket;
import fr.atlasworld.protocol.socket.ServerSocket;

public interface ApiBridge {
    ServerSocket.Builder createServer();
    ClientSocket.Builder createClient();
}
