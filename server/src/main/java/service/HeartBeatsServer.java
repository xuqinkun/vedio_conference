package service;

import common.bean.UserState;
import common.bean.User;
import common.bean.HeartBeatsPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ThreadPoolUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class HeartBeatsServer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(HeartBeatsServer.class);

    private static final ExecutorService executor = ThreadPoolUtil.getExecutorService(10, "HeartBeatsServer");

    MeetingCache meetingCache = MeetingCache.getInstance();

    private Selector selector;

    private ServerSocketChannel serverChannel;

    private boolean stopped;

    public HeartBeatsServer(int port) throws IOException {
        this("0.0.0.0", port);

    }

    public HeartBeatsServer(String host, int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port), 1024);
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            log.warn("Listen on:" + serverChannel.getLocalAddress());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        while (!stopped) {
            try {
                if (selector.select(1000) != 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> it = keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        try {
                            handleInput(key);
                        } catch (IOException e) {
                            log.warn(e.getMessage());
                            key.cancel();
                            key.channel().close();
                            ((SocketChannel)key.channel()).socket().close();
                        }
                        it.remove();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                SocketChannel client = serverChannel.accept();
                if (client == null) {
                    return;
                }
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                String addr = client.getRemoteAddress().toString();
                log.warn("Client[{}] arrived", addr);
            }
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                if (sc.isConnected()) {
                    handleRead(sc);
                }
            }
        }
    }

    private void handleRead(SocketChannel sc) throws IOException {
        HeartBeatsPacket packet = HeartBeatsPacket.deserialize(sc);
        if (packet != null && packet.getState() == UserState.RUNNING) {
            log.warn("Heart beats: [{}]", packet);
            String meetingId = packet.getMeetingId();
            String username = packet.getUsername();
            User user = meetingCache.getUser(meetingId, username);
            if (user == null) {
                log.debug("Invalid heart beats packet[{}], abandon it.", packet);
            } else {
                User cacheUser = meetingCache.getUser(meetingId, username);
                cacheUser.setTimeStamp(System.currentTimeMillis());
            }
        } else {
            // TODO check the remote address, remove the close connection
            log.warn("User state is null or leave, maybe connection is close.");
        }
    }

    public void setStop() {
        stopped = true;
    }
}
