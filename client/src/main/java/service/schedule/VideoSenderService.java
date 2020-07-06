package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FrameRecorder;
import service.network.MessageSender;
import service.network.TcpClient;
import util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoSenderService extends ScheduledService<Image> {
    private Webcam webcam;
    private TcpClient client;
    private boolean started;
    private ExecutorService threadPool;
    private ImageRecorder recorder;

    public VideoSenderService(String output) throws FrameRecorder.Exception {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(320, 240));
        started = false;
        recorder = new ImageRecorder(output);
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
//                    new Thread(client).start();
                    Thread thread = new Thread(recorder);
                    thread.setPriority(Thread.MAX_PRIORITY);
                    thread.start();
                    started = true;
                }
                if (!webcam.isOpen()) {
                    System.out.println("Open WebCam");
                    webcam.open();
                }
                BufferedImage bufferedImage = webcam.getImage();
                byte[] data = ImageUtil.imageToBytes(bufferedImage);
                Image image = new Image(new ByteArrayInputStream(data));
//                client.addMessage(new Message(IMAGE, data.length, data));
                recorder.addImage(bufferedImage);
                return image;
            }
        };
    }

}
