package service.schedule;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ImageUtil;

public class ImagePullTask extends Task<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(ImagePushTask.class);

    private boolean stopped;

    private String inStream;

    private FrameGrabber grabber;

    public ImagePullTask(String inStream, ImageView iv) {
        this.inStream = inStream;
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });
    }

    @Override
    protected Image call() throws Exception {
        while (!stopped) {
            if (grabber == null) {
                TaskHolder<FrameGrabber> grabberHolder = DeviceUtil.getGrabber(inStream);
                if (!grabberHolder.isStarted()) {
                    Thread.sleep(1000);
                    continue;
                }
                grabber = grabberHolder.getTask();
            }
            Frame frame = this.grabber.grab();
            updateValue(ImageUtil.convert(frame));
        }
        return null;
    }
}
