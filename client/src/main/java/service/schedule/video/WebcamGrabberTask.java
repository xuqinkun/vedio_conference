package service.schedule.video;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.SlowTaskHolder;
import util.DeviceManager;
import util.ImageUtil;

import java.awt.image.BufferedImage;

public class WebcamGrabberTask extends Grabber {
    private static final Logger LOG = LoggerFactory.getLogger(WebcamGrabberTask.class);

    private static Webcam webcam;

    private static SlowTaskHolder<Webcam> webcamHolder;

    public WebcamGrabberTask() {
        webcamHolder = DeviceManager.getWebcam();
        webcam = webcamHolder.getContent();
    }

    @Override
    protected Image call() {
        Image image = null;
        try {
            BufferedImage bufferedImage;
            if (webcamHolder.isStarted() && (bufferedImage = webcam.getImage()) != null) {
                VideoContainer.getInstance().addImage(bufferedImage);
                image = ImageUtil.bufferedImage2JavafxImage(bufferedImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}
