package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.ImageUtil;

import java.awt.image.BufferedImage;
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
        long start = 0;
        long videoTS;
        long counter = System.currentTimeMillis();
        while (!stopped) {
            try {
                TaskHolder<Webcam> webcamHolder = DeviceUtil.getWebcam(Config.getCaptureDevice());
                Webcam webcam = webcamHolder.getTask();
                TaskHolder<FrameRecorder> recorderHolder = DeviceUtil.getRecorder(outStream);
                if (!webcamHolder.isStarted() || !recorderHolder.isStarted()) {
                    if (!webcam.isOpen()) {
                        LOG.warn("Open WebCam. Please wait...");
                        webcam.open();
                        webcamHolder.setStarted(true);
                    }
                    if (!recorderHolder.isSubmitted()) {
                        LOG.warn("Submit recorder task. Please wait...");
                        recorderHolder.submit();
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    continue;
                }
                BufferedImage image = webcam.getImage();
                // Update local video
                Frame frame = ImageUtil.convert(image);
                updateValue(ImageUtil.convert(frame));
                // Push video to Stream-Server
                FrameRecorder recorder = recorderHolder.getTask();
                if (start == 0) {
                    start = System.currentTimeMillis();
                }
                LOG.debug("{}ms", (System.currentTimeMillis() - counter));
                counter = System.currentTimeMillis();
                videoTS = (System.currentTimeMillis() - start) * 1000;
                if (recorder.getTimestamp() < videoTS) {
                    recorder.setTimestamp(videoTS);
                }

                recorder.record(frame);
//                Thread.sleep(24);
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
