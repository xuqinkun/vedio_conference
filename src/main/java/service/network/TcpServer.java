package service.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    ServerSocket socket;

    public TcpServer() throws IOException {
        socket = new ServerSocket();
    }

    public void listen() {
        try {
            socket.bind(new InetSocketAddress("localhost", 8888));
            System.out.println("Listen in port " + socket.getLocalPort());
            Socket client = socket.accept();
            System.out.println("Accept remote ip:" + client.getRemoteSocketAddress());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            writer.write("Hello " + client.getRemoteSocketAddress());
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        TcpServer server = new TcpServer();
        server.listen();
    }
}
