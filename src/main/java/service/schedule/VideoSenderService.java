package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

public class VideoSenderService extends ScheduledService<Image> {
    FrameGrabber grabber;
    FrameRecorder recorder;
    Webcam webcam;
    DatagramSocket server;

    public VideoSenderService(String output) {
        webcam = Webcam.getDefault();
        initialize();
    }

    private void initialize() {
        try {
            server = new DatagramSocket(10234);
            server.connect(InetAddress.getByName("192.168.0.105"), 12345);
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
                DatagramPacket packet = new DatagramPacket(data, data.length);
                server.send(packet);
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

    @Override
    protected void cancelled() {
        super.cancelled();
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            if (grabber != null) {
                grabber.stop();
            }
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
