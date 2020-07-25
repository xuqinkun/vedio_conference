package service.schedule.video;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import util.Config;

public class GrabberScheduledService extends ScheduledService<Image> {
    private static final Logger LOG = LoggerFactory.getLogger(GrabberScheduledService.class);

    private String layoutName;

    private String portraitSrc;

    private ImageView localView;

    private ImageView globalView;

    public GrabberScheduledService(ImageView localView, ImageView globalView, String layoutName) {
        this.layoutName = layoutName;
        this.localView = localView;
        this.globalView = globalView;
        initListener();
        setDelay(Duration.millis(0));
        setPeriod(Duration.millis(5));
        portraitSrc = Config.getInstance().getDefaultPortraitSrc();
    }

    protected void initListener() {
        this.valueProperty().addListener((observable, oldValue, image) -> {
            if (image != null) {
                if (!localView.isVisible()) {
                    localView.setVisible(true);
                }
                if (!globalView.isVisible()) {
                    globalView.setVisible(true);
                }
                localView.setImage(image);
                if (layoutName.equals(SessionManager.getInstance().getActiveLayout())) {
                    globalView.setRotate(0);
                    globalView.setImage(image);
                }
            }
        });
        // If service is cancelled, then hide the ImageView
        setOnCancelled(event -> {
            LOG.debug("Service Canceled");
            Image image = new Image(portraitSrc);
            localView.setImage(image);
            globalView.setVisible(false);
        });
    }

    @Override
    protected Task<Image> createTask() {
        return Grabber.createDefault();
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public void setLocalView(ImageView localView) {
        this.localView = localView;
    }

    public void setGlobalView(ImageView globalView) {
        this.globalView = globalView;
    }
}
