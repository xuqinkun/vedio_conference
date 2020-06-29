package service.network;

import service.model.Message;
import service.model.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class TcpClient implements Runnable {
    protected Socket socket;
    protected boolean stopped;
    protected BlockingQueue<Message> msgQueue;
    protected String host;
    protected int port;
    private MessageType type;

    protected TcpClient(int port, MessageType type) {
        this("localhost", port, type);
    }

    protected TcpClient(String host, int port, MessageType type) {
        this.host = host;
        this.port = port;
        socket = new Socket();
        stopped = false;
        msgQueue = new LinkedBlockingQueue<>();
        this.type = type;
    }

    protected void doConnect() {
        try {
            if (!socket.isConnected()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(new Message(type, 0, null));
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connect remote failed");
            System.exit(1);
        }
    }

    public void addMessage(Message message) {
        msgQueue.add(message);
    }

    public Message getNext() throws InterruptedException {
        return msgQueue.take();
    }

    @Override
    public void run() {
        doConnect();
        doRun();
    }

    protected abstract void doRun();
}
