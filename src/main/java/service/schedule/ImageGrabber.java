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
    private BlockingQueue<Image> imageQueue;

    public ImageGrabber(String input) throws FrameGrabber.Exception {
        System.out.println("input:" + input);
        if (input == null) {
            grabber = FrameGrabber.createDefault(0);
        }
        else {
            grabber = new FFmpegFrameGrabber(input);
        }
        stopped = false;
        imageQueue = new LinkedBlockingQueue<>(10);
    }

    public Image getNext() throws InterruptedException {
        return imageQueue.poll(10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            System.out.println("ImageGrabber: Boot grabber");
            grabber.start();
            System.out.println("ImageGrabber: Start grab image");
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (!stopped) {
            try {
                Frame frame = grabber.grab();
                if (frame != null) {
                    Image image = ImageUtil.convert(frame);
                    imageQueue.put(image);
                }
                Thread.sleep(10);
            } catch (FrameGrabber.Exception | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
