package fr.atlasworld.protocol.security;

import fr.atlasworld.protocol.connection.InsecureConnection;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.UUID;

/**
 * Authenticator handles the authentication of clients.
 * <p>
 * <b>Warning:</b> An authenticator is shared and thus runs on multiple threads,
 * this means that if you access fields or external objects,
 * they should be handled with thread safety in mind!
 */
@FunctionalInterface
public interface Authenticator {

    /**
     * Authenticate and determine the public key of the client.
     *
     * @param connection connection.
     * @param uuid unique id of the connection.
     *
     * @return public key determined for the connection.
     */
    PublicKey authenticate(@NotNull InsecureConnection connection, @NotNull UUID uuid);
}
