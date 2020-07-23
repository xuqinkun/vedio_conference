package service.schedule.layout;

import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.video.GrabberScheduledService;
import util.SystemUtil;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class VideoSwitchTask extends Task<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(VideoSwitchTask.class);

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    private static SessionManager sessionManager = SessionManager.getInstance();

    private boolean isOpen;

    private ImageView globalView;

    private Parent videoSwitchBtn;

    private Label label;

    private ImageView videoIcon;

    public VideoSwitchTask(boolean videoIsOpen, Parent videoSwitchBtn, ImageView globalView) {
        this.isOpen = videoIsOpen;
        this.globalView = globalView;
        this.videoSwitchBtn = videoSwitchBtn;

        label = (Label) videoSwitchBtn.lookup("#videoBtnLabel");
        videoIcon = (ImageView) videoSwitchBtn.lookup("#videoIcon");
        exceptionProperty().addListener((observable, oldValue, newValue) -> {
            log.error(newValue.getMessage());
        });
    }

    @Override
    protected void updateValue(Boolean isOpen) {
        super.updateValue(isOpen);
        GrabberScheduledService grabberScheduledService = sessionManager.getGrabberScheduledService();
        if (grabberScheduledService == null) {
            String info = "Please wait for system initializing";
            SystemUtil.showSystemInfo(info);
            return;
        }
        if (isOpen) {
            log.debug("Open video");
            videoIcon.setImage(new Image("/fxml/img/video_on.png"));
            label.setText("Video On");
            if (grabberScheduledService.getState() != State.READY || grabberScheduledService.isRunning()) {
                grabberScheduledService.restart();
            } else {
                grabberScheduledService.start();
            }
        } else {
            log.debug("Close video");
            videoIcon.setImage(new Image("/fxml/img/video_off.png"));
            label.setText("Video Off");
            if (grabberScheduledService.isRunning()) {
                grabberScheduledService.cancel();
            }
        }
    }

    @Override
    protected Boolean call() {
        return isOpen;
    }
}
