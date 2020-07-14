package controller;

import common.bean.HttpResult;
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
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;
import service.schedule.ImagePushTask;
import service.schedule.LoadingTask;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MeetingRoomController implements Initializable {
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
        HttpResult<List<User>> result = HttpClientUtil.getInstance().
                doPost(UrlMap.getUserListUrl(), SessionManager.getInstance().getCurrentMeeting().getUuid());
        List<User> userList = result.getMessage();
        for (User user : userList)
            addUser(user);
    }

    private void hideControl(Pane node) {
        node.setVisible(false);
        node.setManaged(false);

    }

    private void displayControl(Pane node) {
        node.setVisible(true);
        node.setManaged(true);
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
        String portrait = user.getPortrait() == null ? "/fxml/img/orange.png" : user.getPortrait();
        Image image = new Image(portrait);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);

        HBox hBox = new HBox();
        Label label = new Label(user.getName());
        hBox.getChildren().addAll(label);

        VBox vBox = new VBox();
        vBox.setMaxSize(200, 50);
        vBox.getChildren().addAll(imageView, hBox);
        vBox.setStyle("-fx-border-color: red");

        userListLayout.getChildren().add(vBox);
    }

    @FXML
    private RadioButton videoSwitchBtn;

    @FXML
    private ImageView mainImageView;

    private ImagePushTask task;

    @FXML
    public void videoSwitch(ActionEvent event) {
        if (videoSwitchBtn.isSelected()) {
            try {
                if (task == null || task.isDone()) {
                    LoadingTask loadingTask = new LoadingTask(mainImageView);
                    task = new ImagePushTask("rtmp://localhost:1935/live/room", mainImageView, loadingTask);
                    new Thread(loadingTask).start();
                }
                new Thread(task).start();
            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        } else {
            if (task != null) {
                task.stop();
            }
        }
    }

}
