package service.network;

import common.bean.StateType;
import common.bean.UserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.Message;
import service.model.MessageType;
import util.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static service.model.MessageType.IMAGE;
import static service.model.MessageType.TEXT;

public class AsyncClient implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AsyncClient.class);

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stopped;
    private BlockingQueue<UserState> msgSendQueue;

    private long lastRead;
    private long lastWrite;

    public AsyncClient(String host, int port) {
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

    public void addMessage(UserState state) {
        msgSendQueue.add(state);
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
                    UserState userState = msgSendQueue.poll(1, TimeUnit.SECONDS);
                    if (userState == null) {
                        Thread.sleep(1000);
                        continue;
                    }
                    ByteBuffer[] buffers = userState.serialize();
                    while (socketChannel.write(buffers) != 0)
                        ;
                    log.warn("Send image take:" + (System.currentTimeMillis() - lastWrite) + "ms");
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
                    log.warn("Connect[{}] succeed!", sc.getRemoteAddress());
                    sc.register(selector, SelectionKey.OP_READ);
                } else {
                    log.warn("Connect[{}] failed!", sc.getRemoteAddress());
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
            } else if (type == IMAGE) {
                System.out.println("Read image take:" +
                        (System.currentTimeMillis() - lastRead) + "ms");
                lastRead = System.currentTimeMillis();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AsyncClient asyncClient = new AsyncClient(Config.getHeartBeatsServerHost(), Config.getHeartBeatsServerPort());
        Thread thread = new Thread(asyncClient);
        thread.start();

        asyncClient.addMessage(new UserState("123", "test", StateType.RUNNING));

        thread.join();

    }
}
