package fr.atlasworld.protocol.security;

import fr.atlasworld.protocol.connection.InsecureConnection;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HandshakeHandler {

    /**
     * Handle custom authentication logic.
     *
     * @param connection connection.
     * @param ctx context of the authentication.
     */
    void customAuthentication(@NotNull InsecureConnection connection, @NotNull AuthenticationContext ctx);
}
