package fr.atlasworld.protocol.packet;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import fr.atlasworld.protocol.connection.Connection;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketBase implements GenericPacket, Response, Request {
    private static final Map<Class<? extends Message>, WeakReference<Method>> CACHE =
            new ConcurrentHashMap<>();

    private final Header header;
    private final Connection source;
    private final byte[] payload;

    public PacketBase(Header header, Connection source, byte[] payload) {
        this.header = header;
        this.source = source;
        this.payload = payload;
    }

    @Override
    public @NotNull Header header() {
        return this.header;
    }

    @Override
    public @NotNull Connection source() {
        return this.source;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends Message> @NotNull M payload(Class<M> messageType) throws InvalidProtocolBufferException {
        try {
            Method method = this.determineMethod(messageType);
            return (M) method.invoke(null, this.payload);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("Missing method " + messageType.getSimpleName() + " #parseFrom(byte[] data)!");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InvalidProtocolBufferException ex)
                throw ex;

            throw new IllegalArgumentException("Unexpected exception received from parse " +
                    messageType.getSimpleName() + " #parseFrom(byte[] data) method!", cause);
        }
    }

    private Method determineMethod(Class<? extends Message> type) throws NoSuchMethodException {
        WeakReference<Method> reference = CACHE.get(type);

        if (reference == null || reference.get() == null) {
            reference = new WeakReference<>(type.getMethod("parseFrom", byte[].class));

            CACHE.put(type, reference);
        }

        return reference.get();
    }
}
