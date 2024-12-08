package fr.atlasworld.protocol.handler.event;

import fr.atlasworld.protocol.connection.ConnectionImpl;

public record HandshakeFinishedEvent(ConnectionImpl connection) {
}
