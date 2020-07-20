package service.network;

import common.bean.UserState;
import common.bean.HeartBeatsPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class HeartBeatsClient implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HeartBeatsClient.class);

    private String host;
    private int port;
    private boolean stopped;
    private String meetingID;
    private String username;
    private Selector selector;
    private SocketChannel socketChannel;

    private long lastWrite;

    public HeartBeatsClient(String meetingID, String username, String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            this.meetingID = meetingID;
            this.username = username;
            lastWrite = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
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
                if (socketChannel.isConnected()) {
                    HeartBeatsPacket packet = new HeartBeatsPacket(meetingID, username, UserState.RUNNING);
                    ByteBuffer[] buffers = packet.serialize();
                    socketChannel.write(buffers);
                    log.debug("Send packet take:" + (System.currentTimeMillis() - lastWrite) + "ms");
                    lastWrite = System.currentTimeMillis();
                    Thread.sleep(1000);
                } else { // Not register ready
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
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Config config = Config.getInstance();
        config.setUseLocal(true);
        String server = config.getHeartBeatsServerHost();
        int port = config.getHeartBeatsServerPort();
        HeartBeatsClient heartBeatsClient = new HeartBeatsClient("123", "aa", server, port);
        Thread thread = new Thread(heartBeatsClient);
        thread.start();

        thread.join();

    }
}
