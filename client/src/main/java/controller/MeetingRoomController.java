package controller;

import common.bean.Meeting;
import common.bean.User;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.network.HeartBeatsClient;
import service.schedule.layout.*;
import service.schedule.video.GrabberScheduledService;
import util.LayoutUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MeetingRoomController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(5);
    private final SessionManager sessionManager = SessionManager.getInstance();
    @FXML
    private Pane rootLayout;
    @FXML
    private Pane userListLayout;
    @FXML
    private Pane titleBar;
    @FXML
    private Button leaveMeetingBtn;
    @FXML
    private Parent videoSwitchBtn;
    @FXML
    private ImageView globalImageView;
    @FXML
    private Parent audioSwitchBtn;
    @FXML
    private Pane toolbar;
    @FXML
    private Label meetingHostLabel;
    @FXML
    private Label meetingTypeLabel;
    @FXML
    private Label managerIconLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label chatIconLabel;


    private double lastX;
    private double lastY;
    private double oldStageX;
    private double oldStageY;
    private Stage chatStage;
    private Stage invitationStage;
    private MeetingRoomControlTask controlTask;
    private HeartBeatsClient client;
    private TimerService timerService;
    private ManagerNumRefreshService managerNumRefreshService;
    private ChatNumRefreshService chatNumRefreshService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sessionManager.getCurrentMeeting() == null) {
            log.warn("Can't find current meeting info!");
            /* Only for debug */
            User user = new User("aa", "a");
            sessionManager.setCurrentUser(user);
            Meeting meeting = new Meeting();
            meeting.setUuid("test");
            meeting.setHost(user.getName());
            sessionManager.setCurrentMeeting(meeting);
        }
        String username = sessionManager.getCurrentUser().getName();
        String meetingId = sessionManager.getCurrentMeeting().getUuid();
        if (sessionManager.isMeetingHost()) {
            leaveMeetingBtn.setText("End");
        } else {
            leaveMeetingBtn.setText("Leave");
        }
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());

        controlTask = new MeetingRoomControlTask(meetingHostLabel, userListLayout, globalImageView);
        exec.schedule(controlTask, 0, TimeUnit.MILLISECONDS);
        // Start HeartBeats Report
        client = new HeartBeatsClient(meetingId, username);
        exec.schedule(client, 0, TimeUnit.MILLISECONDS);
        initToolbar();
        initStatusBar();
        initRefreshService();
    }

    private void initRefreshService() {
        managerNumRefreshService = new ManagerNumRefreshService(managerIconLabel);
        managerNumRefreshService.setPeriod(Duration.seconds(1));
        managerNumRefreshService.start();
        chatNumRefreshService = new ChatNumRefreshService(chatIconLabel);
        chatNumRefreshService.setPeriod(Duration.seconds(1));
        chatNumRefreshService.start();
    }

    private void initStatusBar() {
        // Status bar
        Meeting currentMeeting = sessionManager.getCurrentMeeting();
        meetingHostLabel.setText(currentMeeting.getHost());
        meetingTypeLabel.setText(currentMeeting.getMeetingType());
        timerService = new TimerService(timeLabel);
        timerService.setPeriod(Duration.seconds(1));
        timerService.start();
    }

    private void initToolbar() {
        for (Node node : toolbar.getChildren()) {
            node.setOnMouseEntered(event -> {
                node.setStyle("-fx-background-color: #dad6d6");
            });
            node.setOnMouseExited(event -> {
                node.setStyle("-fx-background-color: #ffffff");
            });
        }
        leaveMeetingBtn.setOnMouseEntered(event -> {
            leaveMeetingBtn.setStyle("-fx-background-color: red;-fx-text-fill: white;-fx-border-radius:5");
        });
        leaveMeetingBtn.setOnMouseExited(event -> {
            leaveMeetingBtn.setStyle("-fx-background-color: white;-fx-border-color:red; -fx-text-fill: red;-fx-border-radius:5");
        });
    }

    @FXML
    public void openOrCloseChat(MouseEvent event) throws IOException {
        if (chatStage == null) {
            Parent root = LayoutUtil.loadFXML("/fxml/ChatRoom.fxml");
            chatStage = new Stage();
            chatStage.setScene(new Scene(root));
            chatStage.setResizable(false);
            chatStage.show();
        } else if (chatStage.isShowing()) {
            chatStage.close();
        } else {
            chatStage.show();
        }
        event.consume();
    }

    private EventHandler<MouseEvent> handler = event -> {
        new LeaveMeetingService(rootLayout).start();
        GrabberScheduledService grabberScheduledService = sessionManager.getGrabberScheduledService();
        if (grabberScheduledService != null) {
            grabberScheduledService.cancel();
        }
        controlTask.stopMeeting();
        exec.remove(client);
        exec.remove(controlTask);
        timerService.cancel();
        managerNumRefreshService.cancel();
        ManagerLayoutRefreshService refreshService = sessionManager.getRefreshService();
        if (refreshService != null) {
            refreshService.cancel();
        }
    };

    @FXML
    public void leaveMeeting(ActionEvent event) {
        Stage mainStage = (Stage) rootLayout.getScene().getWindow();
        if (sessionManager.isMeetingHost()) {
            // Ask for end meeting else leave meeting, set meeting host before leaving
            String content = "You can appoint a host before leaving or end meeting directly.";
            LayoutUtil.showDialog(mainStage, content, handler);
        } else {
            String content = "Are you sure to leave.";
            LayoutUtil.showDialog(mainStage, content, handler);
        }
        event.consume();
    }

    @FXML
    public void mouseDragEnter(MouseEvent event) {
        lastX = event.getScreenX();
        lastY = event.getScreenY();
    }

    @FXML
    public void mousePress(MouseEvent event) {
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        oldStageX = stage.getX();
        oldStageY = stage.getY();
        lastX = event.getScreenX();
        lastY = event.getScreenY();
    }

    @FXML
    public void mouseDrag(MouseEvent event) {
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        stage.setX(oldStageX + event.getScreenX() - lastX);
        stage.setY(oldStageY + event.getScreenY() - lastY);
    }

    @FXML
    public void videoSwitch(MouseEvent event) {
        new VideoSwitchService(videoSwitchBtn, globalImageView).start();
        event.consume();
    }

    @FXML
    public void audioSwitch(MouseEvent event) {
        new AudioSwitchService(audioSwitchBtn).start();
        event.consume();
    }

    @FXML
    public void invite(MouseEvent event) throws IOException {
        if (invitationStage == null) {
            Parent root = LayoutUtil.loadFXML("/fxml/Invitation.fxml");
            invitationStage = new Stage();
            invitationStage.setTitle("Invitation");
            invitationStage.setResizable(false);
            invitationStage.setScene(new Scene(root));
            invitationStage.show();
        } else if (invitationStage.isShowing()) {
            invitationStage.hide();
        } else {
            invitationStage.show();
        }
        invitationStage.setOnCloseRequest(event1 -> {
            Label infoLabel = (Label) invitationStage.getScene().getRoot().getChildrenUnmodifiable().get(2);
            infoLabel.setText("");
        });
        event.consume();
    }

    private Stage managerViewStage;

    @FXML
    public void viewManager(MouseEvent event) {
        if (managerViewStage == null) {
            Stage mainStage = (Stage) rootLayout.getScene().getWindow();
            Parent root = LayoutUtil.loadFXML("/fxml/ManagerLayout.fxml");
            managerViewStage = new Stage();
            managerViewStage.setResizable(false);
            managerViewStage.setScene(new Scene(root));
            managerViewStage.initOwner(mainStage);
            managerViewStage.show();
        } else if (managerViewStage.isShowing()) {
            managerViewStage.close();
        } else {
            managerViewStage.show();
        }
        event.consume();
    }
}
