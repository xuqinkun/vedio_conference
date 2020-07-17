package service.schedule.audio;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import service.schedule.DeviceHolder;
import util.Config;
import util.DeviceManager;

import javax.sound.sampled.SourceDataLine;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AudioPlayerService extends ScheduledService<byte[]> {

    private FFmpegFrameGrabber audioGrabber;

    private DeviceHolder<FFmpegFrameGrabber> audioGrabberHolder;

    public AudioPlayerService(String inStream) {
        audioGrabberHolder = DeviceManager.getAudioGrabber(inStream);
        audioGrabber = audioGrabberHolder.getDevice();
        init();
    }

    private void init() {
        setDelay(Duration.millis(1000.0 / Config.getRecorderFrameRate()));
        setPeriod(Duration.millis(Config.getRecorderFrameRate()));

        DeviceHolder<SourceDataLine> sourceDataLineHolder = DeviceManager.getSourceDataLineHolder();
        SourceDataLine sourceDataLine = sourceDataLineHolder.getDevice();
        valueProperty().addListener((observable, oldValue, data) -> {
            if (data != null && sourceDataLine.isRunning()) {
                sourceDataLine.write(data, 0, data.length);
            }
        });
    }

    @Override
    protected Task<byte[]> createTask() {
        return new Task<byte[]>() {
            @Override
            protected byte[] call() throws Exception {
                if (audioGrabberHolder.isStarted() && !audioGrabber.isCloseInputStream()) {
                    Frame frame = audioGrabber.grabSamples();
                    Buffer[] samples = frame.samples;
                    if (samples != null && samples.length > 0) {
                        ShortBuffer buffer = (ShortBuffer) samples[0];
                        short[] shorts = new short[buffer.limit()];
                        buffer.get(shorts);
                        byte[] data = new byte[shorts.length * 2];
                        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
                        return data;
                    }
                }
                return null;
            }
        };
    }
}
