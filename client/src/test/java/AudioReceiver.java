import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import service.model.VideoFormat;

import javax.sound.sampled.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class AudioGrabber implements Runnable {
    FFmpegFrameGrabber grabber;
    SourceDataLine mSourceLine;

    public AudioGrabber(String input) {
        grabber = new FFmpegFrameGrabber(input);
        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            mSourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            mSourceLine.open(audioFormat);
            mSourceLine.start();

//            grabber.setOption("fflags", "nobuffer");
//            grabber.setFormat("flv");
//            grabber.setSampleRate(44100);
//            grabber.setFrameRate(24);
//            grabber.setAudioChannels(2);
//            grabber.setAudioOption("crf", "0");
//            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);


            grabber.start();
        } catch (LineUnavailableException | FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Frame frame = grabber.grabSamples();
            if (grabber.hasAudio()) {
                Buffer[] samples = frame.samples;
                if (samples != null && samples.length > 0) {
                    ShortBuffer buffer = (ShortBuffer) samples[0];
                    short[] shorts = new short[buffer.limit()];
                    buffer.get(shorts);
                    byte[] data = new byte[shorts.length * 2];
                    ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
                    mSourceLine.write(data, 0, data.length);
                }
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}


class Test {
    public static void test() throws LineUnavailableException {
        SourceDataLine sourceLine;
        DataLine.Info info = new DataLine.Info(
                SourceDataLine.class, VideoFormat.getAudioFormat());
        sourceLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceLine.open(VideoFormat.getAudioFormat());

        DataLine.Info targetInfo = new DataLine.Info(
                TargetDataLine.class, VideoFormat.getAudioFormat());
        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetLine.open(VideoFormat.getAudioFormat());
        targetLine.start();
        sourceLine.start();
        int size = targetLine.getBufferSize() / 2;
        byte[] buffer = new byte[size];
        while (targetLine.isOpen() && targetLine.read(buffer, 0, size) != -1) {
            sourceLine.write(buffer, 0, size);
        }
    }
}

public class AudioReceiver {

    public static void main(String[] args) {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        AudioGrabber audioGrabber = new AudioGrabber("rtmp://localhost:1935/live/room");
        long frameRate = 24;
        exec.scheduleAtFixedRate(audioGrabber, 1000 / frameRate, (long) 1000 / frameRate,
                TimeUnit.MILLISECONDS);
    }
}
