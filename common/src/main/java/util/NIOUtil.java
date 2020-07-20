package util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOUtil {
    public static final int SIZE_BYTES_NUM = 4;

    public static ByteBuffer channelToBuffer(SocketChannel sc) throws IOException {
        if (!sc.isConnected())
            return null;
        ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(SIZE_BYTES_NUM);
        sc.read(sizeBuffer);
        sizeBuffer.flip();
        int size = sizeBuffer.getInt();

        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(size);
        sc.read(dataBuffer);
        dataBuffer.flip();
        return dataBuffer;
    }
}
