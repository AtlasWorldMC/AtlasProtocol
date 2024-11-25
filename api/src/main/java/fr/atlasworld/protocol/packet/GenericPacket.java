package fr.atlasworld.protocol.packet;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import fr.atlasworld.protocol.connection.Connection;
import fr.atlasworld.protocol.packet.header.Header;
import org.jetbrains.annotations.NotNull;

/**
 * Generic Packet, holds the base packets methods.
 *
 * @param <M> message type.
 */
public interface GenericPacket {

    /**
     * Retrieve the header of the packet.
     *
     * @return the header.
     */
    @NotNull
    Header header();

    /**
     * Retrieve the source of the packet.
     *
     * @return source of the packet.
     */
    @NotNull
    Connection source();

    /**
     * Retrieve the payload as a specific ProtoBuf message.
     *
     * @param messageType message type.
     *
     * @return parsed payload into the message.
     * @throws com.google.protobuf.InvalidProtocolBufferException if the buffer cannot be parsed into the message.
     */
    @NotNull
    <M extends Message> M payload(Class<M> messageType) throws InvalidProtocolBufferException;
}
