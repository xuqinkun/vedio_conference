package util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpUtil {

    public static boolean isReachable(InetSocketAddress addr) {
        Socket socket = new Socket();
        try {
            socket.connect(addr, 1000);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
