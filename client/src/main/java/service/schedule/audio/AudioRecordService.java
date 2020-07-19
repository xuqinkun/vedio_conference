package service.schedule.audio;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.SlowTaskHolder;
import util.Config;
import util.DeviceManager;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioRecordService extends ScheduledService<ShortBuffer> {
    private static final Logger LOG = LoggerFactory.getLogger(AudioRecordService.class);
    private int audioChannels;
    private TargetDataLine targetDataLine;
    private byte[] audioBytes;
    private FFmpegFrameRecorder audioRecorder;
    private SlowTaskHolder<FFmpegFrameRecorder> audioRecorderHolder;
    private SlowTaskHolder<TargetDataLine> targetDataLineHolder;
    int sampleRate;

    public AudioRecordService(String outputStream) {
        this.sampleRate = Config.getAudioSampleRate();
        this.audioChannels = Config.getAudioChannels();
        int audioBufferSize = sampleRate * audioChannels;
        this.audioBytes = new byte[audioBufferSize];
        audioRecorderHolder = DeviceManager.getAudioRecorder(outputStream);
        audioRecorder = audioRecorderHolder.getContent();
        targetDataLineHolder = DeviceManager.getTargetDataLineHolder();
        if (targetDataLineHolder != null) {
            targetDataLine = targetDataLineHolder.getContent();
        }
        init();
    }

    private void init() {
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    audioRecorder.recordSamples(sampleRate, audioChannels, newValue);
                } catch (FrameRecorder.Exception e) {
                    LOG.error(e.getCause().toString());
                }
            }
        });
        setDelay(Duration.millis(0));
        setPeriod(Duration.millis(1000.0 / Config.getRecorderFrameRate()));
    }

    @Override
    protected Task<ShortBuffer> createTask() {
        return new Task<ShortBuffer>() {
            @Override
            protected ShortBuffer call() {
                if (audioRecorderHolder.isStarted() && targetDataLineHolder.isStarted()) {
                    int nBytesRead = targetDataLine.read(audioBytes, 0, targetDataLine.available());
                    int nSamplesRead = nBytesRead / 2;
                    short[] samples = new short[nSamplesRead];
                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    return ShortBuffer.wrap(samples, 0, nSamplesRead);
                }
                return null;
            }
        };
    }
}
