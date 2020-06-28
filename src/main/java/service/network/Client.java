package service.network;

import com.github.sarxos.webcam.Webcam;
import service.model.Message;
import service.model.MessageType;
import service.schedule.VideoReceiverService;

import java.awt.*;
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
import java.util.concurrent.TimeUnit;

import static service.model.MessageType.IMAGE;
import static service.model.MessageType.TEXT;

public class Client implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stopped;
    private BlockingQueue<Message> msgSendQueue;

    private long lastRead;
    private long lastWrite;

    public Client(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            msgSendQueue = new LinkedBlockingQueue<>();
            lastRead = System.currentTimeMillis();
            lastWrite = System.currentTimeMillis();
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
                    while (socketChannel.write(buffers) != 0)
                        ;
                    System.out.println("Send image take:" +
                            (System.currentTimeMillis() - lastWrite) + "ms");
                    lastWrite = System.currentTimeMillis();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        this.stopped = true;
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
            else if (type == IMAGE) {
                VideoReceiverService.getInstance().addImage(message.toImage());
                System.out.println("Read image take:" +
                        (System.currentTimeMillis() - lastRead) + "ms");
                lastRead = System.currentTimeMillis();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client("localhost", 8888);
        Thread thread = new Thread(client);
        thread.start();


        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
        ByteBuffer imageBytes = webcam.getImageBytes();
        byte[] data = new byte[imageBytes.limit()];
        imageBytes.get(data);

        System.out.println(data.length);

        client.addMessage(new Message(IMAGE, data.length, data));

        thread.join();

       /* Scanner sc = new Scanner(System.in);
        String s;
        while ((s = sc.nextLine()) != null) {
            byte[] data = s.getBytes();
            client.addMessage(new Message(TEXT, data.length, data));
        }*/
    }
}
