package service.schedule;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ImageRecorder implements Runnable {
    private FrameRecorder recorder;
    private boolean stopped;
    private BlockingQueue<BufferedImage> imageQueue;

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
        recorder.setOption("probesize", "34");
        recorder.setOption("max_analyze_duration", "10");
    }

    public void addImage(BufferedImage img) throws InterruptedException {
        imageQueue.put(img);
    }

    @Override
    public void run() {
        try {
            System.out.println("Start recorder...1");
            recorder.start();
            System.out.println("Recorder started 1");
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
            System.err.println("Start recorder failed");
            System.exit(1);
        }
        while (!stopped) {
            try {
                BufferedImage image = imageQueue.poll(10, TimeUnit.MILLISECONDS);
                if (image != null) {
                    Frame frame = ImageUtil.convert(image);
                    recorder.record(frame);
                }
            } catch (InterruptedException | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
