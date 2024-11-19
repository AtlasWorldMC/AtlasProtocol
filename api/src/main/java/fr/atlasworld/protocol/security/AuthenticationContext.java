package fr.atlasworld.protocol.security;

import fr.atlasworld.protocol.packet.header.RequestHeader;

import java.util.function.BiConsumer;

/**
 * Authentication context, used by the {@link Authenticator} to handle authentication.
 */
public interface AuthenticationContext {

    /**
     * Add a listener for when a packet is received.
     *
     * @param consumer consumer to execute when a packet is received.
     *
     * @return instance of this context.
     */
    AuthenticationContext onPacketReceived(BiConsumer<RequestHeader, byte[]> consumer);

    /**
     * Add a listener for when the remote disconnects.
     *
     * @param listener listener to execute when the connection ends.
     *
     * @return instance of this context.
     */
    AuthenticationContext onDisconnect(Runnable listener);
}
