package service.schedule.video;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import util.Config;

public abstract class Grabber extends Task<Image> {

    public static Grabber createDefault() {
        if (Config.useWebcam()) {
            return new WebcamGrabberTask();
        } else {
            return new FFmpegGrabberTask();
        }
    }
}
