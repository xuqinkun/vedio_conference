package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

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

    private double lastX;

    private double lastY;

    private double oldStageX;

    private double oldStageY;

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
    }

    private void hideControl(Parent node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    private void displayControl(Parent node) {
        node.setVisible(true);
        node.setManaged(true);
    }

    @FXML
    public void openOrCloseChat() {
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        int addWidth = 8;
        if (openChatBtn.isSelected()) {
            double newSize = rootLayout.getWidth() + chatLayout.getPrefWidth();
            stage.setWidth(newSize + addWidth);
            displayControl(chatLayout);
        } else {
            hideControl(chatLayout);
            stage.setWidth(rootLayout.getWidth() - chatLayout.getWidth() - addWidth);
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
    public void keyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            send();
            sendMessageLabel.requestFocus();
        }
    }

    private void send() {
        String promptText = chatInputArea.getPromptText();
        System.out.println(promptText);
        String text = chatInputArea.getText();
        if (!StringUtils.isEmpty(text.trim())) {
            VBox vBox = new VBox();
            vBox.setSpacing(5);
            Label time = new Label(new Date().toString());
            Label username = new Label("username");
            Label msg = new Label(text);
            double width = chatMessageContainer.getWidth();
            decorate(time, width, 30);
            decorate(username, width, 30);
            username.setStyle("-fx-background-color: green;");
            decorate(msg, width, 30);
            vBox.getChildren().addAll(time, username, msg);
            chatMessageContainer.getChildren().add(vBox);
        }
        chatInputArea.clear();
//        chatInputArea.setPromptText(promptText);
    }

    private void decorate(Label label, double width, int height) {
        label.setPadding(new Insets(5));
        label.setPrefSize(width, height);
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setTextAlignment(TextAlignment.CENTER);
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
}
