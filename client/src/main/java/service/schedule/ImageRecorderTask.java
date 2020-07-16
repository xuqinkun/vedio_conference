package service.schedule;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DeviceUtil;

public class ImageRecorderTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ImageRecorderTask.class);

    private FrameRecorder recorder;
    private boolean stopped;
    private long start;
    private TaskHolder<FrameRecorder> recorderHolder;
    private long videoTS;

    public ImageRecorderTask(String output) {
        recorderHolder = DeviceUtil.getRecorder(output);
        recorder = recorderHolder.getTask();
//        DeviceUtil.initRecorder(output);
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                if (!recorderHolder.isStarted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (start == 0)
                    start = System.currentTimeMillis();
                videoTS = (System.currentTimeMillis() - start) * 1000;
                if (recorder.getTimestamp() < videoTS) {
                    recorder.setTimestamp(videoTS);
                }
                Frame frame = ImageContainer.getInstance().getFrame();
                recorder.record(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
