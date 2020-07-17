package service.schedule.video;

import javafx.scene.image.Image;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.DeviceHolder;
import util.Config;
import util.DeviceManager;
import util.ImageUtil;

public class FFmpegGrabberTask extends Grabber {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegGrabberTask.class);

    private DeviceHolder<FrameGrabber> grabberHolder;

    private FrameGrabber grabber;

    public FFmpegGrabberTask() {
        try {
            grabberHolder = DeviceManager.getVideoGrabber(Config.getCaptureDevice());
            grabber = grabberHolder.getDevice();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Image call() {
        if (grabberHolder.isStarted()) {
            try {
                Frame frame = grabber.grabFrame();
                VideoContainer.getInstance().addFrame(frame);
                return ImageUtil.convert(frame);
            } catch (Exception e) {
                LOG.error(e.getCause().toString());
            }
        }
        return null;
    }
}
