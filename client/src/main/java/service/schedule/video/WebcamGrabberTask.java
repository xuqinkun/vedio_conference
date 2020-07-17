package service.schedule.video;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.TaskHolder;
import util.DeviceManager;
import util.ImageUtil;

import java.awt.image.BufferedImage;

public class WebcamGrabberTask extends Grabber {
    private static final Logger LOG = LoggerFactory.getLogger(WebcamGrabberTask.class);

    private static Webcam webcam;

    private static TaskHolder<Webcam> webcamHolder;

    public WebcamGrabberTask() {
        webcamHolder = DeviceManager.getWebcam();
        webcam = webcamHolder.getTask();
    }

    @Override
    protected Image call() {
        Image image = null;
        if (webcamHolder.isStarted()) {
            try {
                BufferedImage bufferedImage = webcam.getImage();
                VideoContainer.getInstance().addImage(bufferedImage);
                image = ImageUtil.bufferedImage2JavafxImage(bufferedImage);
            } catch (Exception e) {
                e.printStackTrace();
                image = null;
            }
        }
        return image;
    }
}
