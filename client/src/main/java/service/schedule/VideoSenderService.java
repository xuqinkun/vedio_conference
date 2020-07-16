package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import util.DeviceManager;
import util.ImageUtil;

import java.awt.image.BufferedImage;

public class VideoSenderService extends ScheduledService<Image> {
    private Webcam webcam;
    private VideoPushTask recorderTask;

    private TaskHolder<Webcam> webcamHolder;

    public VideoSenderService() {
        webcamHolder = DeviceManager.getWebcam();
        this.webcam = webcamHolder.getTask();
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() {
                if (webcamHolder.isStarted() && webcam.isOpen()) {
                    BufferedImage bufferedImage = webcam.getImage();
                    VideoContainer.getInstance().addImage(bufferedImage);
                    return ImageUtil.bufferedImage2JavafxImage(bufferedImage);
                }
                return null;
            }
        };
    }

}
