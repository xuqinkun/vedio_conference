package service.schedule;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

public abstract class Grabber extends Task<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(Grabber.class);

    protected boolean stopped;

    protected String outStream;

    protected long start = 0;

    public Grabber(String outStream, ImageView iv, ImageLoadingTask imageLoadingTask) {
        this.outStream = outStream;
        initListener(iv, imageLoadingTask);
    }

    public static Grabber createDefault(String outStream, ImageView iv, ImageLoadingTask imageLoadingTask) {
        if (Config.useWebcam()) {
            return new WebcamGrabberTask(outStream, iv, imageLoadingTask);
        } else {
            return new FFmpegGrabberTask(outStream, iv, imageLoadingTask);
        }
    }

    protected void initListener(ImageView iv, ImageLoadingTask imageLoadingTask) {
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (imageLoadingTask != null)
                    imageLoadingTask.cancel();
                iv.setRotate(0);
                iv.setImage(newValue);
            } else {
                LOG.warn("Image is null");
            }
        });
        this.exceptionProperty().addListener((observable, oldValue, newValue) -> LOG.error(newValue.toString()));
    }

    public void stop() {
        this.stopped = true;
    }

    public void reset() {
        this.stopped = false;
    }

    public boolean isStopped() {
        return stopped;
    }

    abstract protected Image call() throws Exception;
}
