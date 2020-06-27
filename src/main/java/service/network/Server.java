package service.network;

import service.model.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private volatile boolean stop;
    Map<String, SocketChannel> clientList;


    public Server(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost", port), 1024);
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        clientList = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            System.out.println("Listen on:" + serverChannel.getLocalAddress());
            while (!stop) {
                try {
                    if (selector.select(1000) == 0)
                        continue;
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> it = keys.iterator();
                    SelectionKey key;
                    while (it.hasNext()) {
                        key = it.next();
                        handleInput(key);
                        it.remove();
                    }
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                String addr = serverChannel.getLocalAddress().toString();
                clientList.put(addr, client);
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
        Message msg = Message.fromChannel(sc);
        if (msg == null) {
            System.out.println("Get message failed!");
            return;
        }
//        MessageType type = msg.getMsgType();
//        if (type == TEXT) {
//            System.out.println("From " + sc.getRemoteAddress());
//            System.out.println(new String(msg.getData()));
//            sc.write(msg.serialize());
//        }
        for (SocketChannel channel: clientList.values()) {
            channel.write(msg.serialize());
        }
    }


    public void stop() {
        this.stop = true;
    }

    public static void main(String[] args) throws IOException {
        new Thread(new Server(8888)).start();
    }
}
