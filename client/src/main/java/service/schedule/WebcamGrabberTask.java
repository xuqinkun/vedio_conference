package service.schedule;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import util.DeviceManager;
import util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public class WebcamGrabberTask extends Grabber {

    private Webcam webcam;

    private TaskHolder<Webcam> webcamHolder;

    public WebcamGrabberTask(String outStream, ImageView iv, ImageLoadingTask imageLoadingTask) {
        super(outStream, iv, imageLoadingTask);
        webcamHolder = DeviceManager.getWebcam();
        this.webcam = webcamHolder.getTask();
    }

    @Override
    protected Image call() throws Exception {
        while (!stopped) {
            try {
                if (!webcamHolder.isStarted()) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    continue;
                }
                BufferedImage image = webcam.getImage();
                updateValue(ImageUtil.bufferedImage2JavafxImage(image));
                VideoContainer.getInstance().addImage(image);
                if (start == 0) {
                    start = System.currentTimeMillis();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            }
        }
        return null;
    }
}
