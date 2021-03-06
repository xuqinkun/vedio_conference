package service.schedule.video;

import javafx.scene.image.Image;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import service.schedule.SlowTaskHolder;
import util.Config;
import util.DeviceManager;
import util.ImageUtil;

public class OpenCVGrabberTask extends Grabber {
    private SlowTaskHolder<FrameGrabber> openCVFrameGrabber;

    private FrameGrabber grabber;

    public OpenCVGrabberTask() {
        openCVFrameGrabber = DeviceManager.getOpenCVFrameGrabber(Config.getInstance().getCaptureDevice());
        grabber = openCVFrameGrabber.getContent();
    }

    @Override
    protected Image call() throws Exception {
        if (openCVFrameGrabber.isStarted()) {
            Frame frame = grabber.grab();
            if (frame != null) {
                VideoContainer.getInstance().addFrame(frame);
                return ImageUtil.convert(frame);
            }
        }
        return null;
    }
}
