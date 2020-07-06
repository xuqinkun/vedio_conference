import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;

/**
 * 测试声音的播放和录制
 */
public class MyAudio {
    public static void main(String args[]) {
        record();
    }

    public static void record() {
        // 5秒后要录音停止
        Thread t = new StopThread();
        t.start();
        try {
            // format
            AudioFormat format = new AudioFormat(8000f, 8, 2, true, false);
            // info, 使用一次就不用了.
            DataLine.Info info = new Info(TargetDataLine.class, format);
            // targetDataLine
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open();
            targetDataLine.start();
            // 长度为 format * targetDataLine 个位
            byte[] buf = new byte[format.getFrameSize() * targetDataLine.getBufferSize() / 8];
            int readBytes = 0;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while (recording) {
                readBytes = targetDataLine.read(buf, 0, buf.length);
                out.write(buf);
            }
            targetDataLine.stop();
            targetDataLine.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
            // 三参 = 一参长度 / 二参帧大小
            AudioInputStream audioIs = new AudioInputStream(bais, format
                    , out.toByteArray().length / format.getFrameSize());
            AudioSystem.write(audioIs, AudioFileFormat.Type.WAVE, new File("test.wav"));
            // 放音
            // format 前面有
            // info
            DataLine.Info sourceInfo = new Info(SourceDataLine.class, format);
            // targetDataLine
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceDataLine.open();
            sourceDataLine.start();
            readBytes = 0;
            audioIs.reset();
            while ((readBytes = audioIs.read(buf, 0, buf.length)) != -1) {
                sourceDataLine.write(buf, 0, readBytes);
            }
            sourceDataLine.stop();
            sourceDataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean recording = true;
}


class StopThread extends Thread {
    @Override
    public void run() {
        try {
            Thread.sleep(10000);
            MyAudio.recording = false;
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}