package fr.atlasworld.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.Map;

/**
 * Server Information, sent when by the server when connecting to the server.
 */
public interface ServerInfo {

    /**
     * Retrieve the server protocol version.
     *
     * @return server protocol version;
     */
    int version();

    /**
     * Retrieve the public key of the server.
     *
     * @return server public key.
     */
    @NotNull
    PublicKey key();

    /**
     * Checks whether the key is present.
     *
     * @param key key to check for.
     *
     * @return true if the key is present, false otherwise.
     */
    boolean has(String key);

    /**
     * Retrieve the value from its key.
     *
     * @param key key.
     *
     * @return value attached to the key,
     *         or {@code null} if no value was attached to the key.
     */
    @Nullable
    String get(String key);

    /**
     * Retrieve all added properties from the server.
     *
     * @return a map containing all sent properties.
     */
    @NotNull
    Map<String, String> properties();
}
