package fr.atlasworld.protocol.handshake;

import fr.atlasworld.common.security.Encryptor;
import fr.atlasworld.protocol.exception.NetworkException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Mac;

// Should not be shared across multiple connections
public interface Handshake {
    boolean finished();
    void destroy();

    void initialize(ChannelHandlerContext ctx) throws NetworkException;
    void handle(ByteBuf packet, ChannelHandlerContext ctx) throws NetworkException;

    Encryptor encryptor();
    Mac signer();
}
