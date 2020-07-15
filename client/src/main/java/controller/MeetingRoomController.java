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
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.messaging.MessageReceiveTask;
import service.model.SessionManager;
import service.schedule.ImagePullTask;
import service.schedule.ImagePushTask;
import service.schedule.ImageLoadingTask;
import util.Config;
import util.JsonUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());
        Meeting currentMeeting = SessionManager.getInstance().getCurrentMeeting();
        if (currentMeeting != null) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
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
            MessageReceiveTask task = new MessageReceiveTask(currentMeeting.getUuid());
            new Thread(task).start();
            task.valueProperty().addListener((observable, oldValue, msg) -> {
                if (msg.getType() == MessageType.USER_ADD) {
                    User user = JsonUtil.jsonToObject(msg.getData(), User.class);
                    addUser(user);
                }
            });
        } else {
            log.warn("Can't find current meeting info!");
        }
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

        if (!user.equals(SessionManager.getInstance().getCurrentUser())) {
            ImagePullTask task = new ImagePullTask(getNginxOutputStream(user.getName()), imageView);
            new Thread(task).start();
        }
    }

    @FXML
    private RadioButton videoSwitchBtn;

    @FXML
    private ImageView mainImageView;

    private ImagePushTask task;

    @FXML
    public void videoSwitch(ActionEvent event) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        if (videoSwitchBtn.isSelected()) {
            if (task == null || task.isDone()) {
                ImageLoadingTask imageLoadingTask = new ImageLoadingTask(mainImageView);
                User user = SessionManager.getInstance().getCurrentUser();
                String outputStream = getNginxOutputStream(user.getName());
                log.warn("OutputStream={}", outputStream);
                task = new ImagePushTask(outputStream, mainImageView, imageLoadingTask);
                executor.schedule(imageLoadingTask, 0, TimeUnit.MILLISECONDS);
            }
            executor.schedule(task, 0, TimeUnit.MILLISECONDS);
            new Thread(task).start();
        } else {
            if (task != null) {
                task.stop();
            }
        }
    }

    private String getNginxOutputStream(String username) {
        Meeting meeting = SessionManager.getInstance().getCurrentMeeting();
        return Config.getNginxOutputStream(meeting.getUuid(), username);
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
