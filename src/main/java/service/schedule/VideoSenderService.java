package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FrameRecorder;
import service.model.Message;
import service.network.MessageSender;
import service.network.TcpClient;
import util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static service.model.MessageType.IMAGE;

public class VideoSenderService extends ScheduledService<Image> {
    private Webcam webcam;
    private TcpClient client;
    private boolean started;
    private ExecutorService threadPool;

    public VideoSenderService(String output) throws FrameRecorder.Exception {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(320, 240));
        started = false;
    }

    public VideoSenderService(int port) {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(320, 240));
        started = false;
        client = new MessageSender(port);
        threadPool = Executors.newCachedThreadPool();
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() throws java.lang.Exception {
                if (!started) {
                    new Thread(client).start();
                    System.out.println("Started MessageSender thread");
                    started = true;
                }
                if (!webcam.isOpen()) {
                    System.out.println("Open WebCam");
                    webcam.open();
                }
                BufferedImage bufferedImage = webcam.getImage();
                byte[] data = ImageUtil.imageToBytes(bufferedImage);
                Image image = new Image(new ByteArrayInputStream(data));
                client.addMessage(new Message(IMAGE, data.length, data));
                return image;
            }
        };
    }

}
