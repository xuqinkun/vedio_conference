package service.schedule;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.DeviceManager;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioPushTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AudioPushTask.class);
    private int audioChannels;
    private int audioBufferSize;
    private TargetDataLine targetDataLine;
    private byte[] audioBytes;
    private long last;
    private FFmpegFrameRecorder audioRecorder;
    private TaskHolder<FFmpegFrameRecorder> audioRecorderHolder;
    private TaskHolder<TargetDataLine> targetDataLineHolder;
    int sampleRate;

    public AudioPushTask(String outputStream) {
        this.sampleRate = Config.getAudioSampleRate();
        this.audioChannels = Config.getAudioChannels();
        this.audioBufferSize = sampleRate * audioChannels;
        this.audioBytes = new byte[audioBufferSize];
        last = System.currentTimeMillis();
        audioRecorderHolder = DeviceManager.getAudioRecorder(outputStream);
        audioRecorder = audioRecorderHolder.getTask();
        targetDataLineHolder = DeviceManager.getTargetDataLineHolder();
        if (targetDataLineHolder != null) {
            targetDataLine = targetDataLineHolder.getTask();
        }
    }


    @Override
    public void run() {
        if (audioRecorderHolder.isStarted() && targetDataLineHolder.isStarted()) {
            int nBytesRead = targetDataLine.read(audioBytes, 0, targetDataLine.available());
            int nSamplesRead = nBytesRead / 2;
            short[] samples = new short[nSamplesRead];
            ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            try {
                audioRecorder.recordSamples(sampleRate, audioChannels, sBuff);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
            last = System.currentTimeMillis();
        }
    }
}
