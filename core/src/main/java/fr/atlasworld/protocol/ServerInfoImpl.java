package fr.atlasworld.protocol;

import fr.atlasworld.protocol.generated.HandshakeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerInfoImpl implements ServerInfo {
    private final int protocolVersion;
    private final Map<String, String> properties;
    private final PublicKey publicKey;

    public ServerInfoImpl(HandshakeWrapper.ServerInfo info, KeyFactory factory) throws InvalidKeySpecException {
        this.protocolVersion = info.getVersion();
        this.properties = info.getPropertiesList().stream()
                .collect(Collectors.toMap(HandshakeWrapper.ServerProperty::getKey, HandshakeWrapper.ServerProperty::getValue));

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(info.getPublicKey().toByteArray());
        this.publicKey = factory.generatePublic(keySpec);
    }

    @Override
    public int version() {
        return this.protocolVersion;
    }

    @Override
    public @NotNull PublicKey key() {
        return this.publicKey;
    }

    @Override
    public boolean has(String key) {
        return this.properties.containsKey(key);
    }

    @Override
    public @Nullable String get(String key) {
        return this.properties.get(key);
    }

    @Override
    public @NotNull Map<String, String> properties() {
        return Map.copyOf(this.properties);
    }
}
