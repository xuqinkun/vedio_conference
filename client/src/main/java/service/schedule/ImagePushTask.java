package service.schedule;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.ImageUtil;

import java.util.concurrent.TimeUnit;

public class ImagePushTask extends Task<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(ImagePushTask.class);

    private boolean stopped;

    private String outStream;

    public ImagePushTask(String outStream, ImageView iv) {
        this(outStream, iv, null);
    }

    public ImagePushTask(String outStream, ImageView iv, ImageLoadingTask imageLoadingTask) {
        this.outStream = outStream;
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (imageLoadingTask != null)
                    imageLoadingTask.cancel();
                iv.setRotate(0);
                iv.setImage(newValue);
            } else {
                iv.setVisible(false);
                LOG.warn("Image is null");
            }
        });
        this.exceptionProperty().addListener((observable, oldValue, newValue) -> LOG.error(newValue.toString()));
    }

    @Override
    protected Image call() throws Exception {
        while (!stopped) {
            try {
                TaskHolder<FrameGrabber> grabberHolder = DeviceUtil.getGrabber(Config.getCaptureDevice());
                TaskHolder<FrameRecorder> recorderHolder = DeviceUtil.getRecorder(outStream);
                if (!grabberHolder.isStarted() || !recorderHolder.isStarted()) {
                    if (!grabberHolder.isSubmitted()) {
                        LOG.warn("Submit grabber task. Please wait...");
                        grabberHolder.submit();
                    }
                    if (!recorderHolder.isSubmitted()) {
                        LOG.warn("Submit recorder task. Please wait...");
                        recorderHolder.submit();
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    continue;
                }
                Frame frame = grabberHolder.getTask().grab();
                // Update local video
                updateValue(ImageUtil.convert(frame));
                // Push video to Stream-Server
                recorderHolder.getTask().record(frame);

                LOG.info("Grab a frame");
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            }
        }
        return null;
    }

    public void stop() {
        this.stopped = true;
    }

    public void reset() {
        this.stopped = false;
    }

    @Override
    public String toString() {
        return "ImagePushTask{" +
                "outStream='" + outStream + '\'' +
                '}';
    }

    public boolean isStopped() {
        return stopped;
    }
}
