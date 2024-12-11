package userend;

import com.google.protobuf.InvalidProtocolBufferException;
import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.common.logging.Level;
import fr.atlasworld.common.logging.LogUtils;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.event.api.executor.EventExecutor;
import fr.atlasworld.protocol.AtlasProtocol;
import fr.atlasworld.protocol.event.EarlyNetworkFailureEvent;
import fr.atlasworld.protocol.event.connection.ConnectionExceptionEvent;
import fr.atlasworld.protocol.event.connection.ConnectionValidatedEvent;
import fr.atlasworld.protocol.socket.ClientSocket;
import fr.atlasworld.registry.RegistryKey;
import fr.atlasworld.registry.SimpleRegistry;
import userend.generated.MessageWrapper;

import java.net.InetSocketAddress;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Client {
    public static final String CLIENT_ID = "cool_identifier";

    private static final byte[] PRIVATE_KEY = new byte[]{
            48, -126, 4, -67, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 4, -89, 48,
            -126, 4, -93, 2, 1, 0, 2, -126, 1, 1, 0, -77, -117, 122, 62, -75, 42, 108, 39, 121, 87, 88, 0, -40, 23, -41,
            -44, 18, -65, 6, -36, 14, -105, 65, 127, 11, 44, -8, 92, 105, 53, -74, 66, 43, 127, -60, 127, -43, -19, 45,
            42, 124, 32, -27, 92, -24, 11, -120, 11, -78, -45, -87, -20, 105, 67, -74, -34, 108, 14, 74, 74, 2, -1, -46,
            24, 8, -58, -39, 98, -20, -43, 50, 8, 95, -103, -59, -87, -107, 122, 8, -18, -125, 98, 38, -8, 78, 16, 13,
            119, 97, 91, -118, 74, -41, 4, -67, 31, -29, 106, -106, -92, -77, -57, 68, 71, 47, 114, 55, -121, 84, -127,
            100, 6, 66, 24, -72, 54, -69, 98, 6, -77, -79, -57, 39, -55, -68, 61, -78, 99, -105, -57, 48, 73, -81, 114,
            -24, 3, 93, 119, -91, -16, -115, -63, 65, 88, 126, 5, -4, 90, 126, 74, 36, -2, -106, -71, 75, 50, 108, 35,
            123, 106, 51, -79, -37, -51, 107, -64, -112, 120, 24, -104, 96, 120, 89, -23, -49, 62, -89, -83, 120, -86,
            -92, 5, 10, 47, -27, -39, -83, 45, 74, 67, 62, -3, -68, 24, 85, 37, 81, 19, 40, -10, 112, 72, 100, 52, -71,
            -99, 72, 16, -57, -123, 57, 107, 53, 87, 36, 63, 31, -60, 115, 42, 98, 41, 126, -39, -97, -68, -67, 35, -95,
            -50, 111, 80, -118, 6, -51, -105, 2, 66, 83, 71, 121, 6, 72, 111, -122, -8, -19, -42, -109, -41, -7, 16, -112,
            43, -96, 121, 2, 3, 1, 0, 1, 2, -126, 1, 0, 71, -68, 125, -66, 90, 82, 53, -34, 91, -68, 11, 60, 99, 26, -69,
            -80, 79, 65, 37, -13, 124, -100, -16, -99, 91, 16, -65, -74, -28, -114, -126, 96, -45, -46, 123, -25, 123,
            -55, 4, -41, -55, -70, -73, 9, -76, -23, 14, -107, 102, 0, -18, 14, -72, 76, 70, -33, 125, -101, 4, 17, -88,
            59, 32, 109, 91, -20, 95, 101, 8, 45, 114, -115, 39, -19, 95, -83, -113, 60, -80, 30, -28, 61, -82, 70, 93,
            -94, 74, 79, 29, -122, 30, -82, 34, 85, 17, 95, -111, 84, -64, 21, -44, -126, -44, -124, 0, -80, 9, 1,
            -72, 64, -18, 127, 21, -97, -100, -46, -71, 113, -60, 45, -67, -27, 77, -65, 107, -57, -42, -29, 10, 66,
            39, 71, 61, -54, -46, -46, -75, 106, 5, 100, -45, -81, -103, 2, -93, -84, 30, 13, -2, -52, -30, 100, -11,
            41, -114, 103, -107, -116, 57, 74, -12, -23, 121, -17, 111, -47, 119, -96, -128, 107, -10, -53, -47, -74,
            101, -57, 0, 64, -15, -55, -85, -109, 116, 43, 78, 98, -106, 5, -35, 57, 86, 58, 29, -37, 52, 55, 37, 109,
            -59, -14, 68, 56, -18, 84, 115, -31, 43, -25, -19, 57, 93, 91, 17, -124, 36, -72, -122, -42, 92, -35, 20,
            109, -124, 77, 88, 93, 56, -68, 97, 126, -34, -46, -76, -35, -58, 6, -113, 9, -14, -39, 94, 7, 9, -14, -15,
            -58, 49, 100, 26, 39, 43, -56, -19, 84, -30, 21, 2, -127, -127, 0, -27, -76, 118, -127, 108, 25, -116, 52,
            119, -53, -94, -43, 98, 8, -44, 40, -103, 101, -36, -60, -18, -25, -23, 82, -61, 43, -109, -62, 124, 36,
            -113, 56, 62, -94, -18, -94, -72, -57, 93, 106, -78, 15, -14, -97, 116, 31, 34, -127, 37, -50, 65, 32, 103,
            -64, 50, 62, -68, 120, -58, -103, -48, 96, -81, 34, -65, 122, 21, -123, 13, -64, 106, -78, 16, 49, 49, -47,
            -64, -17, -46, 101, -123, -35, 72, -96, -123, -12, -105, -6, -69, -53, 56, -6, 10, -99, 112, 14, 40, -126, 0,
            65, 116, -9, -121, 62, 96, -128, 61, -119, 97, -8, 26, -69, 127, 46, 68, -42, -62, -86, -4, 57, 13, -69, -127,
            20, -73, 91, 42, 15, 2, -127, -127, 0, -56, 25, 16, -62, -65, -106, -35, -19, -20, -4, -55, -57, 23, 97, -122,
            127, 17, 38, -125, -38, 16, -22, 57, 58, -123, 33, 106, 108, -7, -61, -75, 23, -81, -128, -33, -61, -111, 122,
            -46, -89, -51, 61, -64, -36, -58, 34, 11, 25, -102, 60, 89, 44, -37, 12, -98, -17, -27, 33, 19, 59, 43, -22,
            44, 105, -51, 105, -28, -22, 87, -5, 96, 62, -56, 16, 13, -12, -115, 115, -99, 24, -44, -54, 116, -64, 125,
            71, 33, -51, -91, -4, 116, -35, 20, -23, -17, 108, 77, -67, -24, -53, 70, -81, -77, 4, 124, -32, 75, 21, -2,
            -120, 2, -111, 48, 38, 23, 18, 102, 107, 37, 22, 16, 91, -6, 95, -119, -91, 52, -9, 2, -127, -128, 103, 29,
            -72, 5, 41, 109, -34, -114, -65, 40, 95, 20, -61, 73, -117, 84, 30, 104, -89, -50, -112, 32, -13, -112, -1,
            -92, 84, 84, 126, 62, 59, 60, -40, -127, 115, -9, 0, 84, 31, 15, -126, -105, -66, 38, -18, -115, -5, -125,
            86, 97, 92, -61, 27, 80, 32, 62, 33, -123, 102, 66, 46, 84, -8, -55, -12, -126, -19, -98, 41, 42, 51, -69,
            115, -95, 97, -25, -13, 96, -75, 102, 117, -51, 124, -120, -13, 25, 118, 16, 76, -75, 82, 2, -115, 57, 87,
            -48, 67, 70, -8, -82, 105, -65, -92, -86, -49, -49, 65, -48, -90, 118, 38, 41, -3, 81, -37, -3, 60, -82, 93,
            67, -113, 114, -104, -29, -97, -36, 0, -23, 2, -127, -128, 38, 30, -120, 79, -31, 121, -41, 59, -115, 75, 55,
            17, 99, -123, -29, -66, 111, 64, -4, 27, 93, -33, -111, 25, 113, -27, 68, -58, 15, -22, -43, -93, -10, 126,
            65, -94, -101, 35, 95, -32, 49, -68, 82, -14, 124, 24, 94, 46, -7, 112, 7, -71, 0, -35, -19, -91, 91, -20, 42,
            34, -70, -95, -8, -26, 11, 8, -30, 96, 54, 116, -96, 8, 53, -54, -63, 45, -42, -120, -80, -29, -37, 29, -38,
            -110, 21, 11, -87, 7, -23, 88, 39, -41, 58, 53, -70, -77, -46, 55, -100, -110, -91, 27, -79, -87, -24, 52,
            62, 121, -87, 44, -75, -8, 124, -56, -76, -38, 127, -126, 103, -27, 69, 38, -112, -109, 4, -64, -31, 79, 2,
            -127, -127, 0, -104, -93, -28, 94, 7, 10, -108, 111, -32, -3, -96, 120, -49, 44, -19, 64, -13, -16, -18, 89,
            -65, 28, 104, 114, 8, -29, 29, -20, 36, -96, -86, 32, -72, -82, -95, 94, -114, 75, -67, -55, 71, 67, 58, 93,
            -91, -41, 35, 25, -110, -83, -19, -68, -123, 43, 31, 61, 105, -69, 12, 70, 50, 74, -99, -17, -48, -59, -63,
            85, 5, 87, 126, -56, 62, 16, 47, 26, 65, 18, -57, -18, -112, 24, -107, 103, -39, -67, 66, 53, -112, -99, 100,
            24, -11, -105, -15, -86, 103, -63, -98, -99, 7, 66, -11, 12, 23, 92, -9, 32, 77, -125, 39, 89, -115, 21, -52,
            -26, 54, 81, 51, 125, -68, 108, 33, -10, -65, -108, -10, 87
    };

    public static void main(String[] args) throws Exception {
        System.setProperty("io.netty.leakDetectionLevel", "ADVANCED");
        LogUtils.setGlobalLevel(Level.TRACE);

        System.out.println("Starting..");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(PRIVATE_KEY);

        PrivateKey key = keyFactory.generatePrivate(keySpec);
        EventNode<Event> root = EventNode.create("root");

        ClientSocket socket = ClientSocket.Builder.create()
                .authenticate(UUID.randomUUID(), key)
                .registry(new SimpleRegistry<>(new RegistryKey("test", "packets")))
                .rootNode(root)
                .connectAddress(new InetSocketAddress("127.0.0.1", AtlasProtocol.DEFAULT_PORT))
                .compatibilityResolver(serverInfo -> {
                    System.out.println("Handshake Property: " + serverInfo.get("data"));
                    return true;
                })
                .build();

        socket.start().join(); // Await start

        root.addListener(EarlyNetworkFailureEvent.class, event -> {
            System.out.println("Early Network Failure: " + event.cause());
        }, builder -> builder.executor(EventExecutor.syncExecutor));

        root.createChildNode("test", Event.class, event -> {
            System.out.println("Event: " + event);
            return true;
        });

        root.addListener(ConnectionExceptionEvent.class, event -> {
                event.cause().printStackTrace();
            }, builder -> builder.executor(EventExecutor.syncExecutor));

        root.addListener(ConnectionValidatedEvent.class, event -> {
            MessageWrapper.Message message = MessageWrapper.Message.newBuilder().setMessage("Hello Server!").build();
            event.connection().sendPacket(new RegistryKey("test", "message"), message)
                    .whenComplete((response, cause) -> {
                        if (cause != null) {
                            System.err.println(cause);
                        }
                        try {
                            System.out.println(response.header().responseCode());

                            String messageResponse = response.payload(MessageWrapper.Message.class).getMessage();
                            System.out.println(messageResponse);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
            });
        }, builder -> builder.executor(EventExecutor.syncExecutor));
    }
}