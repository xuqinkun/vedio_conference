package service.network;

import com.github.sarxos.webcam.Webcam;
import service.schedule.VideoSenderService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import static service.schedule.VideoSenderService.BUFFER_SIZE;

public class UDPClient {
    DatagramSocket sender;
    DatagramSocket receiver;

    public UDPClient() {
        initialize();
    }

    public void initialize() {
        try {
            sender = new DatagramSocket(11111);
            receiver = new DatagramSocket(22222);
        } catch (SocketException e) {
            sender = null;
            receiver = null;
            e.printStackTrace();
        }
    }

    public void send(String msg) throws IOException {
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("0.0.0.0"), 22222);
        sender.send(packet);
    }

    public void send(BufferedImage image) throws IOException {
        byte[] data = VideoSenderService.imageToBytes(image);
        int len = data.length;
        System.out.println("len=" + len);
        int offset = 0;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE + 4);
        byte order = 0;
        byte[] bytes = BytesUtil.int2Bytes(len);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("localhost"), 22222);
        sender.send(packet);
        buffer.clear();
        do {
            if (offset + BUFFER_SIZE < len) {
                buffer.put(order);
                buffer.put(data, offset, BUFFER_SIZE);
                offset += BUFFER_SIZE;
            } else if (offset < len) {
                buffer.put(data, offset, len - offset);
                offset = len;
            }
            packet = new DatagramPacket(buffer.array(), BUFFER_SIZE, InetAddress.getByName("localhost"), 22222);
            sender.send(packet);
//            System.out.println("Send packet[" + order + "] succeed!");
            order++;
            buffer.clear();
        } while (offset < len);
    }

    public void receiveImage() throws IOException {
        byte[] data = new byte[4];
        DatagramPacket response = new DatagramPacket(data, data.length);
        receiver.receive(response);
        int len = BytesUtil.bytes2Int(data);
        byte[] buffer = new byte[BUFFER_SIZE];
        byte[] imageData = new byte[len];

        System.out.println(len);
    }

    public String receive() throws IOException {
        byte[] data = new byte[4];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        receiver.receive(packet);
        int len = BytesUtil.bytes2Int(data);
        System.out.println("len="+len);
        return new String(data, 0, packet.getLength());
    }

    public static void main(String[] args) throws IOException {
        UDPClient client = new UDPClient();
//        client.send("hello sb");
//        System.out.println(client.receive());
        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
        BufferedImage image = webcam.getImage();
        client.send(image);
        client.receiveImage();
    }
}
