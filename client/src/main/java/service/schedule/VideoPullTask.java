package service.schedule;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DeviceManager;
import util.ImageUtil;

public class VideoPullTask extends Task<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegGrabberTask.class);

    private boolean stopped;

    private String inStream;

    private FrameGrabber grabber;

    public VideoPullTask(String inStream, ImageView iv) {
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
                TaskHolder<FrameGrabber> grabberHolder = DeviceManager.getGrabber(inStream);
                if (!grabberHolder.isStarted()) {
                    if (!grabberHolder.isSubmitted()) {
                        LOG.warn("Submit grabber task! Please wait...");
                        grabberHolder.submit();
                    }
                    Thread.sleep(1000);
                    continue;
                }
                LOG.warn("Grabber started!");
                grabber = grabberHolder.getTask();
            }
            Frame frame = this.grabber.grab();
            updateValue(ImageUtil.convert(frame));
        }
        return null;
    }

    @Override
    public String toString() {
        return "ImagePullTask{" +
                "inStream='" + inStream + '\'' +
                '}';
    }
}
