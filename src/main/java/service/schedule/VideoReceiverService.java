package service.schedule;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.bytedeco.javacv.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class VideoReceiverService extends ScheduledService<Image> {
    FrameGrabber grabber;
    boolean isRunning;
    JavaFXFrameConverter converter;
    FrameRecorder recorder;

    public VideoReceiverService() {
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                if (grabber == null) {
                    return null;
                }
                if (!isRunning) {
                    grabber.setOption("stimeout", String.valueOf(1000000));
                    grabber.setTriggerMode(true);
                    grabber.start();
                    isRunning = true;
                }
                Frame frame = grabber.grab();
                if (frame != null)
                    return converter.convert(frame);
                return null;
            }
        };
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            if (grabber != null) {
                grabber.stop();
            }
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
