package javacv;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import javax.swing.*;
import java.io.Console;

import static org.bytedeco.ffmpeg.global.avutil.av_free;

public class GPUAccelerate {

    /**
     * 转码
     *
     * @param input          输入源
     * @param encodeName     编码名称
     * @param inputPixFormat 输入源像素格式
     * @param output         输出地址
     * @param decodeName     解码名称
     * @param width          视频宽度
     * @param height         视频高度
     * @param pixFormat      输出像素格式
     */
    public static void transcode(String input, String encodeName, Integer inputPixFormat, String output, String decodeName, int width, int height, Integer pixFormat) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
        grabber.setVideoCodecName(encodeName);
//        grabber.setFormat("mp4");
        if (inputPixFormat != null)
            grabber.setPixelFormat(inputPixFormat);
        grabber.setImageMode(FrameGrabber.ImageMode.RAW);
        grabber.start();

        double frameRate = grabber.getVideoFrameRate();
        FFmpegFrameRecorder recorder = null;
        if (output != null) {
            recorder = new FFmpegFrameRecorder(output, width, height, 1);
            recorder.setVideoCodecName(decodeName);
            if (pixFormat != null)
                recorder.setPixelFormat(pixFormat);
            recorder.setFrameRate(frameRate);
            recorder.start();
        }

        CanvasFrame canvas = new CanvasFrame("预览");
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        canvas.setVisible(true);
        Frame frame = null;
        for (; (frame = grabber.grab()) != null; ) {
            canvas.showImage(frame);
            if (output != null) {
                recorder.record(frame);
            }
        }
        canvas.dispose();
        recorder.close();
        grabber.close();
    }

    /**
     * 是否支持编码器
     *
     * @param codecName 编码名称
     */
    public static boolean supportEncode(String codecName) {
        AVCodec codec = avcodec.avcodec_find_encoder_by_name(codecName);
        System.out.println(codec);
        try {
            return codec != null;
        } finally {
            av_free(codec);
        }
    }

    /**
     * 是否支持解码器
     *
     * @param codecName 解码名称
     */
    public static boolean supportDecode(String codecName) {
        AVCodec codec = avcodec.avcodec_find_decoder_by_name(codecName);
        System.out.println(codec);
        try {
            return codec != null;
        } finally {
            av_free(codec);
        }
    }

    public static void main(String[] args) throws Exception {
        //nvdia编解码：h264_nvenc,hevc_nvenc, 支持的像素格式：yuv420p nv12 p010le yuv444p p016le yuv444p16le bgr0 rgb0 cuda d3d11
        //intel编解码：hevc_qsv ,h264_qsv,像素格式 ：nv12 p010le qsv,其中qsv像素格式可以解码但是无法编码。
        if (supportEncode("h264_nvenc") && supportDecode("hevc_nvenc")) {
            transcode("E://Video//2.mp4", "h264_nvenc", avutil.AV_PIX_FMT_NV12, "test.mkv", "hevc_nvenc", 640, 480, avutil.AV_PIX_FMT_NV12);
        }
    }
}
