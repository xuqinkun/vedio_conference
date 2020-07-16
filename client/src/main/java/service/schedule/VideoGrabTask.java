package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DeviceManager;
import util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public class VideoGrabTask extends Task<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(VideoGrabTask.class);

    private boolean stopped;

    private String outStream;

    private Webcam webcam;

    private TaskHolder<Webcam> webcamHolder;
    long start = 0;
    long counter = System.currentTimeMillis();

    public VideoGrabTask(String outStream, ImageView iv) {
        this(outStream, iv, null);
    }

    public VideoGrabTask(String outStream, ImageView iv, ImageLoadingTask imageLoadingTask) {
        this.outStream = outStream;
        webcamHolder = DeviceManager.getWebcam();
        this.webcam = webcamHolder.getTask();
        initListener(iv, imageLoadingTask);
    }

    private void initListener(ImageView iv, ImageLoadingTask imageLoadingTask) {
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

    @Override
    protected Image call() throws Exception {
        while (!stopped) {
            try {
                if (!webcamHolder.isStarted()) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    continue;
                }
                BufferedImage image = webcam.getImage();
                VideoContainer.getInstance().addImage(image);
                updateValue(ImageUtil.bufferedImage2JavafxImage(image));
                if (start == 0) {
                    start = System.currentTimeMillis();
                }
                LOG.debug("{}ms", (System.currentTimeMillis() - counter));
                counter = System.currentTimeMillis();
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
