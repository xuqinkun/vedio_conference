package javacv;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.IOException;

import static org.bytedeco.ffmpeg.global.avcodec.av_free_packet;

public class ConvertVideoPacket {
    FFmpegFrameGrabber grabber;
    FFmpegFrameRecorder recorder;
    int width, height;

    int audioCodecID;
    int codecID;
    double frameRate;
    int videoBitRate;
    int audioChannels;
    int audioBitRate;
    int sampleRate;

    public ConvertVideoPacket from(String src) throws FrameGrabber.Exception {
        grabber = new FFmpegFrameGrabber(src);
        if (src.contains("rtsp")) {
            grabber.setOption("rtsp_transport", "tcp");
        }
        grabber.start();// 开始之后ffmpeg会采集视频信息，之后就可以获取音视频信息
        if (width < 0 || height < 0) {
            width = grabber.getImageWidth();
            height = grabber.getImageHeight();
        }
        // 视频参数
        audioCodecID = grabber.getAudioCodec();
        System.err.println("音频编码：" + audioCodecID);
        codecID = grabber.getVideoCodec();
        frameRate = grabber.getVideoFrameRate();// 帧率
        videoBitRate = grabber.getVideoBitrate();// 比特率
        // 音频参数
        // 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
        audioChannels = grabber.getAudioChannels();
        audioBitRate = grabber.getAudioBitrate();
        if (audioBitRate < 1) {
            audioBitRate = 128 * 1000;
        }
        return this;
    }

    public ConvertVideoPacket to(String out) throws IOException {
        recorder = new FFmpegFrameRecorder(out, width, height);
        recorder.setVideoOption("crf", "18");
        recorder.setGopSize(2);
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(videoBitRate);

        recorder.setAudioChannels(audioChannels);
        recorder.setAudioBitrate(audioBitRate);
        recorder.setSampleRate(sampleRate);

        AVFormatContext fc = null;
        if (out.contains("rtmp") || out.contains("flv")) {
            recorder.setFormat("flv");
            recorder.setAudioCodecName("aac");
            recorder.setVideoCodec(codecID);
            fc = grabber.getFormatContext();
        }
        recorder.start(fc);
        return this;
    }

    public void start() {
        long errIndex = 0;
        //连续五次没有采集到帧则认为视频采集结束，程序错误次数超过1次即中断程序
        for (int no_frame_index = 0; no_frame_index < 5 || errIndex > 1; ) {
            AVPacket pkt = null;
            try {
                //没有解码的音视频帧
                pkt = grabber.grabPacket();
                if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    //空包记录次数跳过
                    no_frame_index++;
                    continue;
                }//不需要编码直接把音视频帧推出去
                errIndex += (recorder.recordPacket(pkt) ? 0 : 1);//如果失败err_index自增1
                av_free_packet(pkt);
            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                errIndex++;
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

    }

}
