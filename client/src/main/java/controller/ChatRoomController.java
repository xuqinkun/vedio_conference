package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class ChatRoomController implements Initializable {

    @FXML
    private Pane chatLayout;

    @FXML
    private VBox chatMessageContainer;

    @FXML
    private TextArea chatInputArea;

    @FXML
    private Label sendMessageLabel;

    @FXML
    private ScrollBar chatBoxScrollBar;

    private double chatBoxFillHeight;

    @FXML
    private ChoiceBox<String> receiverChoiceBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    @FXML
    public void sendMessage(MouseEvent event) {
        send();
        sendMessageLabel.requestFocus();
        event.consume();
    }

    @FXML
    public void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            send();
            sendMessageLabel.requestFocus();
        }
    }

    private void send() {
        String text = chatInputArea.getText();
        displayMessage(text);
        chatInputArea.clear();
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
        } else {
            chatBoxFillHeight += height;
        }
        return label;
    }
}
