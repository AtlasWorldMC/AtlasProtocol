package userend;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.protocol.exception.NetworkException;
import fr.atlasworld.protocol.exception.request.PayloadInvalidException;
import fr.atlasworld.protocol.packet.Packet;
import fr.atlasworld.protocol.packet.PacketHandlerContext;
import fr.atlasworld.protocol.packet.Request;
import fr.atlasworld.registry.RegistryKey;
import userend.generated.MessageWrapper;

public class MessagePacket implements Packet {
    public static final RegistryKey KEY = new RegistryKey("test", "message");

    @Override
    public void handle(PacketHandlerContext ctx, Request request) throws NetworkException {
        try {
            System.out.println("Received Message from Client: " + request.payload(MessageWrapper.Message.class).getMessage());
        } catch (InvalidProtocolBufferException e) {
            throw new PayloadInvalidException(request.header().uniqueId(), e);
        }

        ctx.responder().acknowledge();

        try {
            Thread.sleep(150000);
        } catch (InterruptedException e) {}

        MessageWrapper.Message response = MessageWrapper.Message.newBuilder()
                .setMessage("Hello Client!")
                .build();

        ctx.responder().respond(response, (short) 200);
    }
}
