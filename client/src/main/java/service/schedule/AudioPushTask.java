package service.schedule;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private FFmpegFrameRecorder recorder;
    TaskHolder<FFmpegFrameRecorder> taskHolder;
    int sampleRate;

    public AudioPushTask(String outputStream, int sampleRate, int sampleSize, int audioChannels) throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSize, audioChannels, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        targetDataLine.open();
        targetDataLine.start();
        LOG.warn("Audio started");
        this.sampleRate = sampleRate;
        this.audioChannels = audioChannels;
        this.audioBufferSize = sampleRate * audioChannels;
        this.audioBytes = new byte[audioBufferSize];
        last = System.currentTimeMillis();
        taskHolder = DeviceManager.getAudioRecorder(outputStream);
        recorder = taskHolder.getTask();
    }


    @Override
    public void run() {
        if (taskHolder.isStarted()) {
            int nBytesRead = targetDataLine.read(audioBytes, 0, targetDataLine.available());
            int nSamplesRead = nBytesRead / 2;
            short[] samples = new short[nSamplesRead];
            ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            try {
                recorder.recordSamples(sampleRate, audioChannels, sBuff);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
            last = System.currentTimeMillis();
        }
    }
}
