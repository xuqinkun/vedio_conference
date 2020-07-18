package service.schedule.video;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.DeviceHolder;
import util.DeviceManager;
import util.ImageUtil;

public class VideoPlayerService extends ScheduledService<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegGrabberTask.class);

    private String inStream;

    private FFmpegFrameGrabber grabber;

    private DeviceHolder<FFmpegFrameGrabber> videoGrabberHolder;

    public VideoPlayerService(String inStream, ImageView iv) {
        this.inStream = inStream;
        videoGrabberHolder = DeviceManager.getFFmpegFrameGrabber(inStream);
        grabber = videoGrabberHolder.getDevice();
        init(iv);
    }

    private void init(ImageView iv) {
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });

        setDelay(Duration.millis(0));
        setPeriod(Duration.millis(2));
    }

    @Override
    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() {
                try {
                    Frame frame;
                    if (videoGrabberHolder.isStarted() && (frame = grabber.grabFrame()) != null) {
                        LOG.debug("Grabber started!");
                        return ImageUtil.convert(frame);
                    }
                } catch (Exception e) {
                    LOG.error(e.getCause().getMessage());
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
