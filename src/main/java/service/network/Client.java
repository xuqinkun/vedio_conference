package service.network;

import service.model.Message;
import service.model.MessageType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static service.model.MessageType.TEXT;

public class Client implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stopped;
    private BlockingQueue<Message> msgSendQueue;

    public Client(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            msgSendQueue = new LinkedBlockingQueue<>();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void addMessage(Message msg) {
        msgSendQueue.add(msg);
    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (!stopped) {
            try {
                if (socketChannel.isConnected() && !msgSendQueue.isEmpty()) {
                    Message msg = msgSendQueue.remove();
                    ByteBuffer [] buffers = msg.serialize();
                    socketChannel.write(buffers);
                }
               if (selector.select(1000) == 0)
                    continue;
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                SelectionKey key;
                while (it.hasNext()) {
                    key = it.next();
                    handleResponse(key);
                    it.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void handleResponse(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    System.out.println("Connect succeed!");
                    sc.register(selector, SelectionKey.OP_READ);
                } else {
                    System.out.println("Connect failed");
                    System.exit(1);
                }
            }
            if (sc.isConnected() && key.isReadable()) {
                doRead(sc);
            }
        }
    }

    private void doWrite(SocketChannel sc) throws IOException {
        if (sc.isConnected() && sc.finishConnect() && msgSendQueue.size() > 0) {
//            ImageFrame frame = imageSendQueue.remove();
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ImageIO.write(frame.toBufferedImage(), "png", bos);
//            byte[] data = bos.toByteArray();
//            ByteBuffer buffer = ByteBuffer.wrap(data);
//            sc.write(buffer);
        }
    }

    private void doRead(SocketChannel sc) throws IOException {
        if (sc.isConnected()) {
            Message message = Message.fromChannel(sc);
            if (message == null) {
                System.out.println("Read message failed");
                return;
            }
            MessageType type = message.getMsgType();
            if (type == TEXT) {
                System.out.println("From " + sc.getRemoteAddress());
                System.out.println(new String(message.getData()));
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 8888);
        new Thread(client).start();
        Scanner sc = new Scanner(System.in);
        String s;
        while ((s = sc.nextLine()) != null) {
            byte[] data = s.getBytes();
            client.addMessage(new Message(TEXT, data.length, data));
        }
    }
}
