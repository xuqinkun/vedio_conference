package service.schedule.video;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.TaskHolder;
import util.Config;
import util.DeviceManager;
import util.ImageUtil;

public class VideoPullService extends ScheduledService<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegGrabberTask.class);

    private String inStream;

    private FrameGrabber grabber;

    private TaskHolder<FrameGrabber> grabberHolder;

    public VideoPullService(String inStream, ImageView iv) {
        this.inStream = inStream;
        grabberHolder = DeviceManager.getGrabber(inStream);
        grabber = grabberHolder.getTask();
        init(iv);
    }

    private void init(ImageView iv) {
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });
        // Start grabber
        if (!grabberHolder.isSubmitted()) {
            LOG.warn("Submit grabber task! Please wait...");
            grabberHolder.submit();
        }

        setDelay(Duration.millis(0));
        setPeriod(Duration.millis(Config.getRecorderFrameRate()));
    }

    @Override
    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                if (!grabberHolder.isStarted()) {
                    LOG.warn("Grabber started!");
                    return ImageUtil.convert(grabber.grabFrame());
                }
                return null;
            }
        };
    }

    @Override
    public String toString() {
        return "VideoPullService{" +
                "inStream='" + inStream + '\'' +
                '}';
    }
}
