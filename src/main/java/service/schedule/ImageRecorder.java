package service.schedule;

import javafx.scene.image.Image;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import util.ImageUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ImageRecorder implements Runnable {
    private FrameRecorder recorder;
    private boolean stopped;
    private BlockingQueue<Image> imageQueue;

    public ImageRecorder(String output) throws FrameRecorder.Exception {
        System.out.println("Output:" + output);
        recorder = FrameRecorder.createDefault(output, 640, 480);
        stopped = false;
        imageQueue = new LinkedBlockingQueue<>();
        recorder.setVideoOption("crf", "18");
        recorder.setGopSize(60);
        recorder.setVideoBitrate(2000000);
        recorder.setVideoOption("tune","zerolatency");
        recorder.setFormat("flv");
        recorder.setFrameRate(30);
    }

    public void addImage(Image img) throws InterruptedException {
        imageQueue.put(img);
    }

    @Override
    public void run() {
        try {
            System.out.println("Start recorder...");
            recorder.start();
            System.out.println("Recorder started");
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
            System.err.println("Start recorder failed");
            System.exit(1);
        }
        while (!stopped) {
            try {
                Image image = imageQueue.take();
                Frame frame = ImageUtil.convert(image);
                recorder.record(frame);
            } catch (InterruptedException | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
