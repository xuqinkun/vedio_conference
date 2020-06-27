package service.network;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClient {
    Socket socket;

    public TcpClient() {
        socket = new Socket();
    }

    public void receive() {
        try {
//            socket.bind(new InetSocketAddress("", ));
            socket.connect(new InetSocketAddress("localhost", 8888), 2000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new DataInputStream(socket.getInputStream())));
            System.out.println(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TcpClient client = new TcpClient();
        client.receive();
    }
}
