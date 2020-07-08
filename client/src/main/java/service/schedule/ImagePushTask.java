package service.schedule;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ImageUtil;

import java.util.concurrent.TimeUnit;

public class ImagePushTask extends Task<Image> {
    private boolean stopped;
    private String outStream;

    private static final Logger LOG = LoggerFactory.getLogger(ImagePushTask.class);

    public ImagePushTask(String outStream, ImageView iv) throws FrameGrabber.Exception, FrameRecorder.Exception {
//        recorder = DeviceUtil.getRecorder(outStream);
//        grabber = DeviceUtil.getGrabber(0);
//        TaskStarter.submit(recorder);
//        TaskStarter.submit(grabber);
//        recorder.start();
//        grabber.start();
        this.outStream = outStream;

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (iv != null) {
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
                TaskHolder<FrameGrabber> grabberHolder = DeviceUtil.getGrabber(0);
                TaskHolder<FrameRecorder> recorderHolder = DeviceUtil.getRecorder(outStream);
                if (!grabberHolder.isStarted() || !recorderHolder.isStarted()) {
                    LOG.warn("Grabber or recorder has not started yet!");
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
}
