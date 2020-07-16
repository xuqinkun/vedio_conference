package service.schedule;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.DeviceUtil;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class ImageRecorderTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ImageRecorderTask.class);

    private FFmpegFrameRecorder recorder;
    private boolean stopped;
    private long start;
    private TaskHolder<FFmpegFrameRecorder> recorderHolder;
    private long videoTS;
    private long counter;
    TargetDataLine targetDataLine;
    byte[] audioBytes;

    public ImageRecorderTask(String output) {
        LOG.warn(output);
        recorderHolder = DeviceUtil.getRecorder(output);
        recorder = recorderHolder.getTask();

        int sampleRate = Config.getAudioSampleRate();

        int numChannels = Config.getAudioSampleSize();
        AudioFormat audioFormat = new AudioFormat(sampleRate,
                numChannels, Config.getAudioChannels(), true, false);

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open();
            targetDataLine.start();
            int audioBufferSize = sampleRate * numChannels;
            audioBytes = new byte[audioBufferSize];
            LOG.warn("Started audio");
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        counter = System.currentTimeMillis();
        if (start == 0)
            start = System.currentTimeMillis();
        videoTS = (System.currentTimeMillis() - start) * 1000;
        if (recorder.getTimestamp() < videoTS) {
            recorder.setTimestamp(videoTS);
        }
        Frame frame = ImageContainer.getInstance().getFrame();
        try {
            recorder.record(frame);
            ShortBuffer sample = getSample();
            if (sample != null) {
                recorder.recordSamples(Config.getAudioSampleRate(), Config.getAudioChannels(), sample);
            }
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        counter = System.currentTimeMillis();
    }

    public ShortBuffer getSample() {
        int nBytesRead = targetDataLine.read(audioBytes, 0, targetDataLine.available());
        int nSamplesRead = nBytesRead / 2;
        short[] samples = new short[nSamplesRead];
        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
        return ShortBuffer.wrap(samples, 0, nSamplesRead);
    }

    public void stop() {
        stopped = true;
    }
}
