package controller;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.MessageType;
import common.bean.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.messaging.MessageReceiveTask;
import service.model.SessionManager;
import service.schedule.*;
import util.Config;
import util.DeviceManager;
import util.JsonUtil;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MeetingRoomController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    @FXML
    private Pane rootLayout;

    @FXML
    private Pane userListLayout;

    @FXML
    private Pane titleBar;

    @FXML
    private RadioButton openChatBtn;

    private double lastX;

    private double lastY;

    private double oldStageX;

    private double oldStageY;

    private Stage chatStage;

    private SessionManager sessionManager = SessionManager.getInstance();

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(10);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());
        if (sessionManager.getCurrentUser() == null) { // For test
            sessionManager.setCurrentUser(new User("aa", "a"));
            Meeting meeting = new Meeting();
            meeting.setUuid("test");
            sessionManager.setCurrentMeeting(meeting);
        }
        Meeting currentMeeting = sessionManager.getCurrentMeeting();
        if (currentMeeting != null) {
            initUserList(currentMeeting);
            listenUserListChange(currentMeeting);
        } else {
            log.warn("Can't find current meeting info!");
        }
        initializeDevice();
    }

    private void listenUserListChange(Meeting currentMeeting) {
        MessageReceiveTask task = new MessageReceiveTask(currentMeeting.getUuid());
        new Thread(task).start();
        task.valueProperty().addListener((observable, oldValue, msg) -> {
            if (msg.getType() == MessageType.USER_ADD) {
                User user = JsonUtil.jsonToObject(msg.getData(), User.class);
                addUser(user);
            }
        });
    }

    private void initUserList(Meeting currentMeeting) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentMeeting.getOwner().equals(currentUser.getName())) { // Is meeting owner
            addUser(currentUser);
        } else { // None meeting owner
            HttpResult<String> result = HttpClientUtil.getInstance().
                    doPost(UrlMap.getUserListUrl(), currentMeeting.getUuid());
            List<User> userList = JsonUtil.jsonToList(result.getMessage(), User.class);
            log.warn(userList.toString());
            for (User user : userList)
                addUser(user);
        }
    }

    private void initializeDevice() {
        log.warn("Initializing devices...");
        String username = sessionManager.getCurrentUser().getName();
        // Initialize video recorder
        exec.schedule(() -> {
            DeviceManager.initVideoRecorder(getVideoOutputStream(username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio recorder
        exec.schedule(() -> {
            DeviceManager.initAudioRecorder(getAudioOutputStream(username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio target
        exec.schedule(DeviceManager::initAudioTarget, 0, TimeUnit.MILLISECONDS);
        // Initialize video grabber
        exec.schedule((Runnable) DeviceManager::initGrabber, 0, TimeUnit.MILLISECONDS);
    }

    @FXML
    public void openOrCloseChat() throws IOException {
        if (openChatBtn.isSelected()) {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/ChatRoom.fxml"));
            chatStage = new Stage();
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } else if (chatStage != null) {
            chatStage.close();
            chatStage = null;
        }
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

    public void addUser(User user) {
        String portrait = user.getPortrait() == null ? Config.getDefaultPortrait() : user.getPortrait();
        Image image = new Image(portrait);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);

        HBox hBox = new HBox();
        Label label = new Label(user.getName());
        hBox.getChildren().addAll(label);

        VBox vBox = new VBox();
        vBox.setId(user.getName());
        vBox.setMaxSize(200, 50);
        vBox.getChildren().addAll(imageView, hBox);
        vBox.setStyle("-fx-border-color: red");

        userListLayout.getChildren().add(vBox);

        log.warn("User[{}] add to list", user);

        if (!user.equals(sessionManager.getCurrentUser())) {
            VideoPullTask task = new VideoPullTask(getVideoOutputStream(user.getName()), imageView);
            new Thread(task).start();
        }
    }

    @FXML
    private RadioButton videoSwitchBtn;

    @FXML
    private ImageView mainImageView;

    private VideoPushTask videoPushTask;

    private Grabber grabber;

    @FXML
    public void videoSwitch(ActionEvent event) {
        if (videoSwitchBtn.isSelected()) {
            log.warn("Open video");
            if (videoPushTask == null) {
                ImageLoadingTask imageLoadingTask = new ImageLoadingTask(mainImageView);
                User user = sessionManager.getCurrentUser();
                String outputStream = getVideoOutputStream(user.getName());
                grabber = Grabber.createDefault(outputStream, mainImageView, imageLoadingTask);
                videoPushTask = new VideoPushTask(outputStream);
                exec.scheduleAtFixedRate(videoPushTask, 0, Config.getRecorderFrameRate(), TimeUnit.MILLISECONDS);
            } else if (grabber.isStopped()) {
                grabber.reset();
            }
            exec.schedule(grabber, 0, TimeUnit.MILLISECONDS);
        } else {
            log.warn("Close video");
            if (grabber != null) {
                grabber.stop();
                exec.remove(grabber);
            }
        }
    }

    @FXML
    private RadioButton audioSwitchBtn;

    private AudioPushTask audioPushTask;

    @FXML
    public void audioSwitch(ActionEvent event) throws LineUnavailableException {
        if (audioSwitchBtn.isSelected()) {
            log.warn("Audio open");
            if (audioPushTask == null) {
                User user = sessionManager.getCurrentUser();
                String outputStream = getAudioOutputStream(user.getName());
                audioPushTask = new AudioPushTask(outputStream);
            }
            exec.scheduleAtFixedRate(audioPushTask, 1000 / Config.getRecorderFrameRate(),
                    Config.getRecorderFrameRate(), TimeUnit.MILLISECONDS);
        } else {
            log.warn("Audio close");
            exec.remove(audioPushTask);
        }
    }

    public String getVideoOutputStream(String username) {
        Meeting meeting = sessionManager.getCurrentMeeting();
        return Config.getNginxOutputStream(meeting.getUuid(), username) + "-video";
    }

    public String getAudioOutputStream(String username) {
        Meeting meeting = sessionManager.getCurrentMeeting();
        return Config.getNginxOutputStream(meeting.getUuid(), username) + "-audio";
    }

    private Stage invitationStage;

    @FXML
    private RadioButton inviteBtn;

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
    }
}
