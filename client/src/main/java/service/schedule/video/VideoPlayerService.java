package service.schedule.video;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.SlowTaskHolder;
import util.DeviceManager;
import util.ImageUtil;

public class VideoPlayerService extends ScheduledService<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegGrabberTask.class);

    private final String inStream;

    private final String layoutName;

    private final ImageView localView;

    private final ImageView globalView;

    private final FFmpegFrameGrabber grabber;

    private final SlowTaskHolder<FFmpegFrameGrabber> videoGrabberHolder;

    private String portraitSrc;

    public VideoPlayerService(String inStream, ImageView localView, ImageView globalView, String layoutName) {
        this.inStream = inStream;
        this.layoutName = layoutName;
        this.localView = localView;
        this.globalView = globalView;
        videoGrabberHolder = DeviceManager.getFFmpegFrameGrabber(inStream);
        grabber = videoGrabberHolder.getContent();
        portraitSrc = SessionManager.getInstance().getPortraitSrc(layoutName);
        init();
    }

    private void init() {
        valueProperty().addListener((observable, oldValue, image) -> {
            if (image != null) {
                localView.setImage(image);
                if (layoutName.equals(SessionManager.getInstance().getActiveLayout())) {
                    globalView.setImage(image);
                    if (!globalView.isVisible())
                        globalView.setVisible(true);
                }
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
