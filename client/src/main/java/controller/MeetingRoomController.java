package controller;

import common.bean.Meeting;
import common.bean.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.network.HeartBeatsClient;
import service.schedule.layout.AudioSwitchTask;
import service.schedule.layout.LeaveMeetingService;
import service.schedule.layout.MeetingRoomControlTask;
import service.schedule.layout.VideoSwitchTask;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MeetingRoomController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(5);

    @FXML
    private Pane rootLayout;
    @FXML
    private Pane userListLayout;
    @FXML
    private Pane titleBar;
    @FXML
    private RadioButton openChatBtn;
    @FXML
    private Button leaveMeetingBtn;
    @FXML
    private RadioButton videoSwitchBtn;
    @FXML
    private ImageView globalImageView;
    @FXML
    private RadioButton audioSwitchBtn;
    @FXML
    private RadioButton inviteBtn;

    private double lastX;
    private double lastY;
    private double oldStageX;
    private double oldStageY;
    private Stage chatStage;
    private Stage invitationStage;

    private final SessionManager sessionManager = SessionManager.getInstance();

    private MeetingRoomControlTask controlTask;
    private HeartBeatsClient client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sessionManager.getCurrentMeeting() == null) {
            log.warn("Can't find current meeting info!");
            /* Only for debug */
            User user = new User("aa", "a");
            sessionManager.setCurrentUser(user);
            Meeting meeting = new Meeting();
            meeting.setUuid("test");
            meeting.setOwner(user.getName());
            sessionManager.setCurrentMeeting(meeting);
        }
        String username = sessionManager.getCurrentUser().getName();
        String meetingId = sessionManager.getCurrentMeeting().getUuid();
        if (sessionManager.isMeetingOwner()) {
            leaveMeetingBtn.setText("End Meeting");
        } else {
            leaveMeetingBtn.setText("Leave Meeting");
        }
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());
        controlTask = new MeetingRoomControlTask(userListLayout, globalImageView);
        exec.schedule(controlTask, 0, TimeUnit.MILLISECONDS);
        // Start HeartBeats Report
        client = new HeartBeatsClient(meetingId, username);
        exec.schedule(client, 0, TimeUnit.MILLISECONDS);
    }

    @FXML
    public void openOrCloseChat() throws IOException {
        if (openChatBtn.isSelected()) {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ChatRoom.fxml"));
            chatStage = new Stage();
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } else if (chatStage != null) {
            chatStage.close();
            chatStage = null;
        }
    }

    @FXML
    public void leaveMeeting(ActionEvent event) {
        try {
            if (sessionManager.isMeetingOwner()) {
                // Ask for end meeting else leave meeting, set meeting host before leaving
                showDialog("You can appoint a host before leaving or end meeting directly.");
            } else {
                showDialog("Are you sure to leave.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.consume();
    }

    private void showDialog(String content) throws IOException {
        Stage mainStage = (Stage) rootLayout.getScene().getWindow();
        Parent dialog = FXMLLoader.load(getClass().getResource("/fxml/Dialog.fxml"));
        Label titleLabel = (Label) dialog.lookup("#titleLabel");
        Label contentLabel = (Label) dialog.lookup("#contentLabel");
        Button cancelBtn = (Button) dialog.lookup("#cancelBtn");
        Button confirmBtn = (Button) dialog.lookup("#confirmBtn");
        titleLabel.setText("Leave Meeting");
        contentLabel.setText(content);
        Stage dialogStage = new Stage();
        dialogStage.setScene(new Scene(dialog));
        dialogStage.initOwner(mainStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        confirmBtn.setOnMouseClicked(event -> {
            dialogStage.close();
            new LeaveMeetingService(rootLayout).start();
            sessionManager.getGrabberScheduledService().cancel();
            controlTask.stopMeeting();
            exec.remove(client);
            exec.remove(controlTask);
        });
        cancelBtn.setOnMouseClicked((event) -> {
            dialogStage.close();
        });
        dialogStage.show();
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
    public void videoSwitch(ActionEvent event) {
        boolean selected = videoSwitchBtn.isSelected();
        exec.schedule(new VideoSwitchTask(selected, globalImageView), 0, TimeUnit.MILLISECONDS);
        event.consume();
    }

    @FXML
    public void audioSwitch(ActionEvent event) {
        boolean selected = audioSwitchBtn.isSelected();
        exec.schedule(new AudioSwitchTask(selected), 0, TimeUnit.MILLISECONDS);
        event.consume();
    }

    @FXML
    public void invite(ActionEvent event) throws IOException {
        if (invitationStage == null) {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Invitation.fxml"));
            invitationStage = new Stage();
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
            inviteBtn.setSelected(false);
        });
        event.consume();
    }
}
