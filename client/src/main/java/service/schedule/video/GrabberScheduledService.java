package service.schedule.video;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.ImageLoadingTask;

public class GrabberScheduledService extends ScheduledService<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(GrabberScheduledService.class);

    public GrabberScheduledService(ImageView iv, ImageLoadingTask imageLoadingTask) {
        initListener(iv, imageLoadingTask);
        setDelay(Duration.millis(0));
        setPeriod(Duration.millis(5));
    }

    protected void initListener(ImageView iv, ImageLoadingTask imageLoadingTask) {
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (imageLoadingTask != null)
                    imageLoadingTask.cancel();
                if (!iv.isVisible()) {
                    iv.setVisible(true);
                }
                iv.setRotate(0);
                iv.setImage(newValue);
            }
        });
        // If service is cancelled, then hide the ImageView
        setOnCancelled(event -> iv.setVisible(false));
//        exceptionProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                LOG.error(newValue.toString());
//            }
//        });
    }

    @Override
    protected Task<Image> createTask() {
        return Grabber.createDefault();
    }
}
