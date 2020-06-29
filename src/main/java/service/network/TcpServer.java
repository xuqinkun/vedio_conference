package service.network;

import service.model.Message;
import service.model.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static service.model.MessageType.*;

public class TcpServer implements Runnable {
    private ServerSocket serverSocket;
    private boolean stopped;
    private ExecutorService threadPool;
    private String host;
    private int port;
    private Map<String, Socket> senderList;
    private Map<String, Socket> receiverList;

    public TcpServer(int port) throws IOException {
        this("localhost", port);
    }

    public TcpServer(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        serverSocket = new ServerSocket();
        stopped = false;
        senderList = new HashMap<>();
        receiverList = new HashMap<>();
        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        new Thread(new ConnectionMonitor()).start();
        try {
            serverSocket.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Listen in port " + serverSocket.getLocalPort());

        while (!stopped) {
            try {
                Socket client = serverSocket.accept();
                login(client);
                threadPool.submit(new MessageDispatcher(client));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String login(Socket client) throws IOException, ClassNotFoundException {
        String addr = client.getRemoteSocketAddress().toString();
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        Message message = (Message) ois.readObject();
        MessageType msgType = message.getMsgType();
        System.out.println("Login:" + addr + " " + msgType);
        if (msgType == SENDER && !senderList.containsKey(addr)) {
            senderList.put(addr, client);
        }
        else if (msgType == RECEIVER && !receiverList.containsKey(addr)) {
            receiverList.put(addr, client);
        }
        else if(msgType == INVALID) {
            System.err.println("Invalid message");
        }
        else {
            System.out.println("Transmit message: " + msgType);
        }
        return addr;
    }

    private class ConnectionMonitor implements Runnable {

        @Override
        public void run() {
            try {
                while (!stopped) {
                    cleanClosedSocket(senderList);
                    cleanClosedSocket(receiverList);
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synchronized void cleanClosedSocket(Map<String, Socket> socketMap) {
            for (String key : socketMap.keySet()) {
                Socket socket = socketMap.get(key);
                if (!socket.isConnected() || socket.isClosed()) {
                    socketMap.remove(key);
                }
            }
        }
    }

    private class MessageDispatcher implements Runnable {
        Socket socket;

        public MessageDispatcher(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                while (!stopped) {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Message message = (Message) ois.readObject();
                    for (String key : receiverList.keySet()) {
//                    if (!key.equals(addr)) {
                        Socket socket = receiverList.get(key);
                        if (socket.isConnected() && !socket.isClosed()) {
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(message);
                            oos.flush();
                        }
//                    }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        new Thread(new TcpServer(8888)).start();
    }
}
