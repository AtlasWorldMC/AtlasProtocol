package fr.atlasworld.protocol;

import fr.atlasworld.common.reflection.ReflectionFactory;
import fr.atlasworld.protocol.internal.ApiBridge;
import org.jetbrains.annotations.ApiStatus;

/**
 * AtlasProtocol base file.
 * You can retrieve the protocol version and supported versions here.
 */
public final class AtlasProtocol {

    /**
     * The current protocol version.
     */
    public static final int PROTOCOL_VERSION = 1;

    /**
     * Supported protocol versions.
     */
    public static final int[] SUPPORTED_PROTOCOL_VERSION = { 1 };

    @ApiStatus.Internal
    public static final ApiBridge BRIDGE = ReflectionFactory.loadSingleService(ApiBridge.class);

    private AtlasProtocol() {
        throw new UnsupportedOperationException();
    }
}
