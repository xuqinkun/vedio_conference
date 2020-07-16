package service.schedule;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

public class ImageRecorder implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ImageRecorder.class);
    private FrameRecorder recorder;
    private boolean stopped;
    private long start;

    public ImageRecorder(String output) throws FrameRecorder.Exception {
        LOG.warn("Output:" + output);
        recorder = new FFmpegFrameRecorder(output, Config.getCaptureImageWidth(), Config.getCaptureImageHeight());
//        recorder.setInterleaved(true);
        // 加上音频延迟显著升高
//        recorder.setAudioChannels(1);
//        recorder.setAudioOption("crf", "0");
//        recorder.setAudioQuality(0);
//        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//        recorder.setOption("fflags", "nobuffer");
//        recorder.setSampleRate(44100);
//        recorder = FrameRecorder.createDefault(output, Config.getCaptureImageWidth(), Config.getCaptureImageHeight());
//        recorder = new FFmpegFrameRecorder(output, Config.getCaptureImageWidth(), Config.getCaptureImageHeight());
        stopped = false;
        recorder.setVideoOption("crf", "18");
        // 该编码格式会增加延迟
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setGopSize(5);
        recorder.setVideoBitrate(2000000);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setFormat("flv");
        recorder.setFrameRate(30);
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setOption("probesize", "102400");  // Max bytes for reading video frame
        recorder.setOption("max_analyze_duration", "5"); // Max duration for analyzing video frame
        recorder.start();
        LOG.warn("Recorder stared");
    }

    @Override
    public void run() {
        long videoTS;
        while (!stopped) {
            try {
                Frame frame = ImageContainer.getInstance().getFrame();
                if (start == 0)
                    start = System.currentTimeMillis();
                videoTS = (System.currentTimeMillis() - start) * 1000;
                if (recorder.getTimestamp() < videoTS) {
                    recorder.setTimestamp(videoTS);
                }
                recorder.record(frame);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
