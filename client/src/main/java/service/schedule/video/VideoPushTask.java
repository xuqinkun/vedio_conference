package service.schedule.video;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.TaskHolder;
import util.DeviceManager;

public class VideoPushTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(VideoPushTask.class);

    private FFmpegFrameRecorder recorder;
    private long start;
    private TaskHolder<FFmpegFrameRecorder> recorderHolder;

    public VideoPushTask(String output) {
        LOG.warn(output);
        recorderHolder = DeviceManager.getVideoRecorder(output);
        recorder = recorderHolder.getTask();
    }

    @Override
    public void run() {
        if (recorderHolder.isStarted()) {
            if (start == 0)
                start = System.currentTimeMillis();
            long videoTS = (System.currentTimeMillis() - start) * 1000;
            if (recorder.getTimestamp() < videoTS) {
                recorder.setTimestamp(videoTS);
            }
            try {
                Frame frame = VideoContainer.getInstance().getFrame();
                if (frame != null) {
                    recorder.record(frame);
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
