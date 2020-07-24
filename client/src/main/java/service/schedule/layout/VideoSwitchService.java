package service.schedule.layout;

import common.bean.Message;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.messaging.MessageSender;
import service.model.SessionManager;
import service.schedule.video.GrabberScheduledService;
import util.SystemUtil;

import static common.bean.OperationType.VIDEO_CLOSE;
import static common.bean.OperationType.VIDEO_ON;

public class VideoSwitchService extends Service<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(VideoSwitchService.class);

    private static SessionManager sessionManager = SessionManager.getInstance();

    private Label label;

    private ImageView videoIcon;

    public VideoSwitchService(Parent videoSwitchBtn, ImageView globalView) {
        label = (Label) videoSwitchBtn.lookup("#videoBtnLabel");
        videoIcon = (ImageView) videoSwitchBtn.lookup("#videoIcon");

        exceptionProperty().addListener((observable, oldValue, newValue) -> {
            log.error(newValue.getMessage());
        });

        valueProperty().addListener((observable, oldValue, newValue) -> {
            updateValue(newValue);
        });
    }

    protected void updateValue(Boolean isOpen) {
        if (isOpen) {
            if (!sessionManager.hasPermission(VIDEO_ON, true)) {
                return;
            }
            GrabberScheduledService grabberScheduledService = sessionManager.getGrabberScheduledService();
            if (grabberScheduledService == null) {
                String info = "Please wait for system initializing";
                SystemUtil.showSystemInfo(info);
                return;
            }
            log.debug("Open video");
            videoIcon.setImage(new Image("/fxml/img/video_on.png"));
            label.setText("Video On");
            if (grabberScheduledService.getState() != State.READY || grabberScheduledService.isRunning()) {
                grabberScheduledService.restart();
            } else {
                grabberScheduledService.start();
            }
        } else {
            GrabberScheduledService grabberScheduledService = sessionManager.getGrabberScheduledService();
            if (grabberScheduledService == null) {
                return;
            }
            log.debug("Close video");
            videoIcon.setImage(new Image("/fxml/img/video_off.png"));
            label.setText("Video Off");
            if (grabberScheduledService.isRunning()) {
                grabberScheduledService.cancel();
            }
            String meetingId = sessionManager.getCurrentMeeting().getUuid();
            String userName = sessionManager.getCurrentUser().getName();
            MessageSender.getInstance().send(meetingId, new Message(VIDEO_CLOSE, userName));
        }
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return label.getText().contains("Off");
            }
        };
    }
}
