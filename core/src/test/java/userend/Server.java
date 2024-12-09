package userend;

import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.common.logging.Level;
import fr.atlasworld.common.logging.LogUtils;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.protocol.socket.ServerSocket;
import fr.atlasworld.registry.RegistryKey;
import fr.atlasworld.registry.SimpleRegistry;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Server {
    private static final byte[] PUBLIC_KEY = new byte[]{
            48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10,
            2, -126, 1, 1, 0, -77, -117, 122, 62, -75, 42, 108, 39, 121, 87, 88, 0, -40, 23, -41, -44, 18, -65, 6, -36,
            14, -105, 65, 127, 11, 44, -8, 92, 105, 53, -74, 66, 43, 127, -60, 127, -43, -19, 45, 42, 124, 32, -27, 92,
            -24, 11, -120, 11, -78, -45, -87, -20, 105, 67, -74, -34, 108, 14, 74, 74, 2, -1, -46, 24, 8, -58, -39, 98,
            -20, -43, 50, 8, 95, -103, -59, -87, -107, 122, 8, -18, -125, 98, 38, -8, 78, 16, 13, 119, 97, 91, -118, 74,
            -41, 4, -67, 31, -29, 106, -106, -92, -77, -57, 68, 71, 47, 114, 55, -121, 84, -127, 100, 6, 66, 24, -72, 54,
            -69, 98, 6, -77, -79, -57, 39, -55, -68, 61, -78, 99, -105, -57, 48, 73, -81, 114, -24, 3, 93, 119, -91, -16,
            -115, -63, 65, 88, 126, 5, -4, 90, 126, 74, 36, -2, -106, -71, 75, 50, 108, 35, 123, 106, 51, -79, -37, -51,
            107, -64, -112, 120, 24, -104, 96, 120, 89, -23, -49, 62, -89, -83, 120, -86, -92, 5, 10, 47, -27, -39, -83,
            45, 74, 67, 62, -3, -68, 24, 85, 37, 81, 19, 40, -10, 112, 72, 100, 52, -71, -99, 72, 16, -57, -123, 57, 107,
            53, 87, 36, 63, 31, -60, 115, 42, 98, 41, 126, -39, -97, -68, -67, 35, -95, -50, 111, 80, -118, 6, -51, -105,
            2, 66, 83, 71, 121, 6, 72, 111, -122, -8, -19, -42, -109, -41, -7, 16, -112, 43, -96, 121, 2, 3, 1, 0, 1
    };

    public static void main(String[] args) throws Exception {
        System.setProperty("io.netty.leakDetectionLevel", "ADVANCED");
        LogUtils.setGlobalLevel(Level.TRACE);

        System.out.println("Starting..");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(PUBLIC_KEY);

        PublicKey key = keyFactory.generatePublic(keySpec);
        EventNode<Event> rootNode = EventNode.create("root");

        ServerSocket socket = ServerSocket.Builder.create()
                .authenticator(((connection, id) -> key))
                .registry(new SimpleRegistry<>(new RegistryKey("test", "packet")))
                .rootNode(rootNode)
                .keepAlive(true)
                .handshakeProperties("date", "dummy") // Dummy data for testing
                .build();

        socket.start().join(); // Wait on the server to boot.
    }
}