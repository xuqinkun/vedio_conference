package controller;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.MessageType;
import common.bean.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import service.schedule.audio.AudioPlayerService;
import service.schedule.layout.AudioSwitchTask;
import service.schedule.layout.VideoSwitchTask;
import service.schedule.video.VideoPlayerService;
import util.Config;
import util.DeviceManager;
import util.JsonUtil;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MeetingRoomController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(10);

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
    @FXML
    private RadioButton videoSwitchBtn;
    @FXML
    private ImageView mainImageView;
    @FXML
    private RadioButton audioSwitchBtn;
    private Stage invitationStage;
    @FXML
    private RadioButton inviteBtn;
    private Map<String, VideoPlayerService> videoPullServiceMap = new HashMap<>();
    private Map<String, AudioPlayerService> audioPlayerServiceMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());
        exec.schedule(this::init, 0, TimeUnit.MILLISECONDS);
    }

    private void init() {
        Meeting currentMeeting = sessionManager.getCurrentMeeting();
        if (currentMeeting != null) {
            initializeDevice();
            initUserList(currentMeeting);
            listenUserListChange(currentMeeting);
        } else {
            log.warn("Can't find current meeting info!");
        }
        // Only for debug
        if (sessionManager.getCurrentUser() == null || currentMeeting == null) {
            User user = new User("aa", "a");
            sessionManager.setCurrentUser(user);
            Meeting meeting = new Meeting();
            meeting.setUuid("test");
            meeting.setOwner(user.getName());
            sessionManager.setCurrentMeeting(meeting);
            // Display user list
            initializeDevice();
            initUserList(meeting);
        }
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
            DeviceManager.initVideoRecorder(Config.getVideoOutputStream(username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio recorder
        exec.schedule(() -> {
            DeviceManager.initAudioRecorder(Config.getAudioOutputStream(username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio target
        exec.schedule(DeviceManager::initAudioTarget, 0, TimeUnit.MILLISECONDS);
        // Initialize audio player
        exec.schedule(DeviceManager::initAudioPlayer, 0, TimeUnit.MILLISECONDS);
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
        imageView.setFitWidth(180);
        imageView.setFitHeight(130);

        HBox hBox = new HBox();
        Label label = new Label(user.getName());
        label.setStyle("-fx-text-fill: white;-fx-background-color: #000000");
        label.setPrefSize(100, 20);
        hBox.setPadding(new Insets(0, 0, 0, 5));
        hBox.getChildren().addAll(label);

        VBox vBox = new VBox();
        vBox.setId(user.getName());
        vBox.setPrefSize(240, 150);
        vBox.setMaxSize(240, 150);
        vBox.getChildren().addAll(imageView, hBox);
        vBox.setPadding(new Insets(5));
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-border-color: #9B9EA4;-fx-background-color: #424446;-fx-border-radius: 5;-fx-background-radius: 5");

        userListLayout.getChildren().add(vBox);

        log.warn("User[{}] add to list", user);

        String userName = user.getName();
        if (!user.equals(sessionManager.getCurrentUser())) {
            startVideoPlayer(imageView, userName);
            startAudioPlayer(userName);
        }
    }

    private void startAudioPlayer(String userName) {
        AudioPlayerService audioPlayerService;
        if (!audioPlayerServiceMap.containsKey(userName)) {
            audioPlayerService = new AudioPlayerService(Config.getAudioOutputStream(userName));
            audioPlayerServiceMap.put(userName, audioPlayerService);
        } else {
            audioPlayerService = audioPlayerServiceMap.get(userName);
        }
        if (!audioPlayerService.isRunning()) {
            audioPlayerService.restart();
            ;
        } else {
            audioPlayerService.start();
        }
    }

    private void startVideoPlayer(ImageView imageView, String userName) {
        VideoPlayerService videoPlayerService;
        if (!videoPullServiceMap.containsKey(userName)) {
            videoPlayerService = new VideoPlayerService(Config.getVideoOutputStream(userName), imageView);
            videoPullServiceMap.put(userName, videoPlayerService);
        } else {
            videoPlayerService = videoPullServiceMap.get(userName);
        }
        if (!videoPlayerService.isRunning()) {
            videoPlayerService.restart();
        } else {
            videoPlayerService.start();
        }
    }

    @FXML
    public void videoSwitch(ActionEvent event) {
        boolean selected = videoSwitchBtn.isSelected();
        exec.schedule(new VideoSwitchTask(selected, mainImageView), 0, TimeUnit.MILLISECONDS);
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
