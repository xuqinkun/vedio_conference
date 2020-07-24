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

    private boolean stopped;
    private String meetingID;
    private String username;
    private Selector selector;
    private SocketChannel socketChannel;

    private long lastWrite;

    public HeartBeatsClient(String meetingID, String username) {
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
                        try {
                            handleResponse(key);
                        } catch (IOException e) {
                            log.error(e.getMessage());
                            key.cancel();
                            key.channel().close();
                            ((SocketChannel)key.channel()).socket().close();
                        }
                        it.remove();
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                try {
                    socketChannel.close();
                    socketChannel.socket().close();
                    stop();
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
    }

    public void stop() {
        log.warn("HeartBeatsClient[{}] stopped.", meetingID);
        this.stopped = true;
    }

    private void doConnect() throws IOException {
        String host = Config.getInstance().getHeartBeatsServerHost();
        int port = Config.getInstance().getHeartBeatsServerPort();
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
                    log.warn("Connection to {} established!", sc.getRemoteAddress());
                    sc.register(selector, SelectionKey.OP_READ);
                } else {
                    log.warn("Failed to establish connection with {}!", sc.getRemoteAddress());
                    System.exit(1);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Config config = Config.getInstance();
        config.setUseLocal(true);
        HeartBeatsClient heartBeatsClient = new HeartBeatsClient("123", "aa");
        Thread thread = new Thread(heartBeatsClient);
        thread.start();
        thread.join();
    }
}
