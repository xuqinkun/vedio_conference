package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import service.model.ImageFrame;
import service.model.Message;
import service.network.Client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static service.model.MessageType.IMAGE;

public class VideoSenderService extends ScheduledService<Image> {
    public static final int BUFFER_SIZE = 10240;
    Webcam webcam;
    DatagramSocket server;
    private Client client;

    public VideoSenderService(Client client) {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        initialize();
        this.client = client;
    }

    private void initialize() {
        try {
            server = new DatagramSocket(10234);
            server.connect(InetAddress.getByName("192.168.0.104"), 12345);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() throws java.lang.Exception {
                if (!webcam.isOpen()) {
                    webcam.open();
                }
                BufferedImage image = webcam.getImage();
                byte[] data = imageToBytes(image);
                client.addMessage(new Message(IMAGE, data.length, data));
                return new Image(new ByteArrayInputStream(data));
            }
        };
    }

    public static byte[] imageToBytes(BufferedImage img) {
        if (img == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }

}
