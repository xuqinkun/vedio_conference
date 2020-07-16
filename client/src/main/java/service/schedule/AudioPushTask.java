package service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioPushTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AudioPushTask.class);
    private int numChannels;
    private int audioBufferSize;
    private TargetDataLine targetDataLine;
    private byte[] audioBytes;
    private long last;

    public AudioPushTask(int sampleRate, int sampleSize, int audioChannels) throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSize, audioChannels, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        targetDataLine.open();
        targetDataLine.start();
        LOG.warn("Audio started");
        this.numChannels = audioFormat.getChannels();
        this.audioBufferSize = sampleRate * numChannels;
        this.audioBytes = new byte[audioBufferSize];
        last = System.currentTimeMillis();
    }


    @Override
    public void run() {
        int nBytesRead = targetDataLine.read(audioBytes, 0, targetDataLine.available());
        int nSamplesRead = nBytesRead / 2;
        short[] samples = new short[nSamplesRead];
        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
        ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
        AudioContainer.getInstance().addSample(sBuff);
//        LOG.warn("{}ms", System.currentTimeMillis() - last);
        last = System.currentTimeMillis();
    }
}
