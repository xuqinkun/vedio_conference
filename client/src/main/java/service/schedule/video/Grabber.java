package service.schedule.video;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import util.Config;

import static util.Config.*;

public abstract class Grabber extends Task<Image> {

    public static Grabber createDefault() {
        int captureType = Config.getCaptureType();
        switch (captureType) {
            case WEBCAM:
                return new WebcamGrabberTask();
            case OPENCV_GRABBER:
                return new OpenCVGrabberTask();
            case FFMPEG_GRABBER:
                return new FFmpegGrabberTask();
            default:
                return null;
        }
    }
}
