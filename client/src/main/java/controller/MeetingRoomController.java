package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import service.model.User;
import service.schedule.ImagePushTask;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MeetingRoomController implements Initializable {
    @FXML
    private Pane rootLayout;

    @FXML
    private Pane userListLayout;

    @FXML
    private Pane chatLayout;

    @FXML
    private Pane titleBar;

    @FXML
    private RadioButton openChatBtn;

    @FXML
    private ChoiceBox<String> receiverChoiceBox;

    @FXML
    private TextArea chatInputArea;

    @FXML
    private Label sendMessageLabel;

    @FXML
    private VBox chatMessageContainer;

    @FXML
    private ScrollBar chatBoxScrollBar;

    private double lastX;

    private double lastY;

    private double oldStageX;

    private double oldStageY;

    private double chatBoxFillHeight;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideControl(userListLayout);
        hideControl(chatLayout);
        rootLayout.setPrefWidth(rootLayout.getPrefWidth() - userListLayout.getPrefWidth() - chatLayout.getPrefWidth());
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());
        receiverChoiceBox.getItems().add("All");
        receiverChoiceBox.getSelectionModel().selectFirst();
        chatInputArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.isEmpty(newValue)) {
                sendMessageLabel.setTextFill(Paint.valueOf("#1972F8"));
            } else {
                sendMessageLabel.setTextFill(Paint.valueOf("#999999"));
            }
        });

        chatBoxScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            chatMessageContainer.setLayoutY(-newValue.doubleValue());
        });
    }

    private void hideControl(Pane node) {
        node.setVisible(false);
        node.setManaged(false);

        int addWidth = 8;
        Scene scene = rootLayout.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.setWidth(rootLayout.getWidth() - chatLayout.getWidth() - addWidth);
        }
    }

    private void displayControl(Pane node) {
        node.setVisible(true);
        node.setManaged(true);
        int addWidth = 8;

        Scene scene = rootLayout.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.setWidth(rootLayout.getWidth() + chatLayout.getPrefWidth() + addWidth);
        }
    }

    @FXML
    public void openOrCloseChat() {
        if (openChatBtn.isSelected()) {
            displayControl(chatLayout);
        } else {
            hideControl(chatLayout);
        }
    }

    @FXML
    public void mouseDragEnter(MouseEvent event) {
        lastX = event.getScreenX();
        lastY = event.getScreenY();
    }

    @FXML
    public void sendMessage(MouseEvent event) {
        send();
        sendMessageLabel.requestFocus();
    }

    @FXML
    public void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            send();
            sendMessageLabel.requestFocus();
            String src = "file:/F:/Projects/JavaWorkplace/video_conference/client/src/main/resources/fxml/img/orange.png";
            User user = new User("xxxx", null, "xxx", src);
            addUser(user);
            if (!userListLayout.isVisible()) {
                displayControl(userListLayout);
            }
        }
    }

    private void send() {
//        String promptText = chatInputArea.getPromptText();
        String text = chatInputArea.getText();
        displayMessage(text);
        chatInputArea.clear();
//        chatInputArea.setPromptText(promptText);
    }

    private void displayMessage(String text) {
        if (!StringUtils.isEmpty(text.trim())) {
            VBox vBox = new VBox();
            vBox.setSpacing(5);

            double width = chatMessageContainer.getWidth();
            int labelHeight = 30;
            Label time = decorate(width, labelHeight, new Date().toString());
            Label username = decorate(width, labelHeight, "username");
            Label msg = decorate(width, labelHeight, text);
            username.setStyle("-fx-text-fill: green;");

            vBox.getChildren().addAll(time, username, msg);
            chatMessageContainer.getChildren().add(vBox);
        }
    }

    private Label decorate(double width, int height, String str) {
        Label label = new Label(str);
        label.setPadding(new Insets(5));
        label.setPrefSize(width, height);
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setTextAlignment(TextAlignment.CENTER);

        /** ScrollBar*/
        if (chatBoxFillHeight + height > chatMessageContainer.getPrefHeight()) {
            chatMessageContainer.setLayoutY(chatMessageContainer.getLayoutY() - height);
//            chatBoxScrollBar.setVisible(true);
        } else {
            chatBoxFillHeight += height;
        }
        return label;
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
        Image image = new Image(user.getPortrait());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        HBox hBox = new HBox();
        Label label = new Label(user.getUsername());
        hBox.getChildren().addAll(label);
        VBox vBox = new VBox();
        vBox.setMaxSize(50, 50);
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
            // Start record video
            try {
                if (task == null || task.isDone()) {
                    task = new ImagePushTask("rtmp://localhost:1935/live/room", mainImageView);
                }
                new Thread(task).start();
            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        } else {
            // Stop record video
            if (task != null) {
                task.stop();
            }
        }
    }

}
