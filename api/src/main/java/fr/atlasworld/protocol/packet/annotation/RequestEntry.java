package fr.atlasworld.protocol.packet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Request entry,
 * used to annotate a field inside a {@link fr.atlasworld.protocol.packet.Packet} to be parsed later into a protobuf to be sent in a packet.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestEntry {

    /**
     * The key of the entry in which the value of the field should be parsed in.
     */
    String key();
}
