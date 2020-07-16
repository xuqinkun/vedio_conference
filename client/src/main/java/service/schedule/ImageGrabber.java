package service.schedule;

import javafx.scene.image.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import util.ImageUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ImageGrabber implements Runnable {
    private FrameGrabber grabber;
    private boolean stopped;

    public ImageGrabber(String input) throws FrameGrabber.Exception {
        System.out.println("input:" + input);
        if (input == null) {
            grabber = FrameGrabber.createDefault(0);
        }
        else {
            grabber = new FFmpegFrameGrabber(input);
        }
        stopped = false;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Frame frame = grabber.grab();
                Image image = ImageUtil.convert(frame);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
