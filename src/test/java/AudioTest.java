import service.model.VideoFormat;

import javax.sound.sampled.*;

public class AudioTest {

    public static void main(String[] args) throws LineUnavailableException {
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
//        sourceLine.start();
        int size = targetLine.getBufferSize() / 2;
        byte[] buffer = new byte[size];
        while (targetLine.isOpen() && targetLine.read(buffer, 0, size) != -1) {
            sourceLine.write(buffer, 0, size);
        }
    }
}
