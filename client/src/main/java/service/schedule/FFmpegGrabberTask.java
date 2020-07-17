package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.DeviceManager;
import util.ImageUtil;

import java.util.concurrent.TimeUnit;

public class FFmpegGrabberTask extends Grabber {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegGrabberTask.class);

    private TaskHolder<FrameGrabber> grabberHolder;

    private FrameGrabber grabber;

    public FFmpegGrabberTask(String outStream, ImageView iv, ImageLoadingTask imageLoadingTask) {
        super(outStream, iv, imageLoadingTask);
        try {
            grabberHolder = DeviceManager.getGrabber(Config.getCaptureDevice());
            grabber = grabberHolder.getTask();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        initListener(iv, imageLoadingTask);
    }

    @Override
    protected Image call() throws Exception {
        while (!stopped) {
            try {
                if (!grabberHolder.isStarted()) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    continue;
                }
                Frame frame = grabber.grabFrame();
                Image image = ImageUtil.convert(frame);
                updateValue(image);
                VideoContainer.getInstance().addFrame(frame);
                if (start == 0) {
                    start = System.currentTimeMillis();
                }
            } catch (Exception e) {
                LOG.error(e.getCause().toString());
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "FFmpegGrabberTask{" +
                "outStream='" + outStream + '\'' +
                '}';
    }
}
