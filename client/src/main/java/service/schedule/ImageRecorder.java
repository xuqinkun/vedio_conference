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
    private long start;

    public ImageRecorder(String output) throws FrameRecorder.Exception {
        System.out.println("Output:" + output);
        recorder = FrameRecorder.createDefault(output, 640, 480);
        stopped = false;
        imageQueue = new LinkedBlockingQueue<>();
        recorder.setVideoOption("crf", "18");
        recorder.setGopSize(60);
        recorder.setVideoBitrate(2000000);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setFormat("flv");
        recorder.setFrameRate(30);
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setOption("probesize", "1024");  // Max bytes for reading video frame
        recorder.setOption("max_analyze_duration", "5"); // Max duration for analyzing video frame
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
        long videoTS;
        long counter = System.currentTimeMillis();
        while (!stopped) {
            try {
                BufferedImage image = imageQueue.poll(10, TimeUnit.MILLISECONDS);
                if (image != null) {
                    if (start == 0)
                        start = System.currentTimeMillis();
                    videoTS = (System.currentTimeMillis() - start) * 1000;
                    if (recorder.getTimestamp() < videoTS) {
                        recorder.setTimestamp(videoTS);
                    }
                    Frame frame = ImageUtil.convert(image);
                    recorder.record(frame);
                    System.out.println(System.currentTimeMillis() - counter);
                    counter = System.currentTimeMillis();
                }
            } catch (InterruptedException | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
