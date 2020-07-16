package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FrameRecorder;
import service.network.MessageSender;
import service.network.TcpClient;
import util.Config;
import util.DeviceUtil;
import util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoSenderService extends ScheduledService<Image> {
    private Webcam webcam;
    private ImageRecorderTask recorderTask;

    private TaskHolder<Webcam> webcamHolder;

    public VideoSenderService() {
        webcamHolder = DeviceUtil.getWebcam(Config.getCaptureDevice());
        this.webcam = webcamHolder.getTask();
    }

    private void initRecorder(String output) {
        recorderTask = new ImageRecorderTask(output);
        Thread thread = new Thread(recorderTask);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        DeviceUtil.initRecorder(output);
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() {
                if (webcamHolder.isStarted() && webcam.isOpen()) {
                    BufferedImage bufferedImage = webcam.getImage();
                    ImageContainer.getInstance().addImage(bufferedImage);
                    return ImageUtil.bufferedImage2JavafxImage(bufferedImage);
                }
                return null;
            }
        };
    }

}
