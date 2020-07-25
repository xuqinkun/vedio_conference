package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.layout.ChatDisplayService;
import service.schedule.layout.ChatSenderService;
import service.schedule.layout.ReceiverChoiceBoxService;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatRoomController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomController.class);

    public static final String ALL = "All";
    @FXML
    private TextArea chatInputArea;

    @FXML
    private Label sendMessageLabel;

    @FXML
    private ScrollPane chatBoxScrollPane;

    @FXML
    private ChoiceBox<String> receiverChoiceBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        receiverChoiceBox.getItems().add(ALL);
        receiverChoiceBox.getSelectionModel().selectFirst();
        chatInputArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.isEmpty(newValue)) {
                sendMessageLabel.setTextFill(Paint.valueOf("#1972F8"));
            } else {
                sendMessageLabel.setTextFill(Paint.valueOf("#999999"));
            }
        });
        VBox chatMessageBox = new VBox();
        chatMessageBox.setPrefSize(chatBoxScrollPane.getPrefWidth() - 10,
                chatBoxScrollPane.getPrefHeight() - 10);
        chatMessageBox.setStyle("-fx-background-color: white");
        chatBoxScrollPane.setContent(chatMessageBox);

        new ReceiverChoiceBoxService(receiverChoiceBox).start();
        ChatDisplayService chatDisplayService = new ChatDisplayService(chatMessageBox);
        chatDisplayService.setDelay(Duration.millis(0));
        chatDisplayService.setPeriod(Duration.millis(200));
        chatDisplayService.start();
    }

    @FXML
    public void sendMessage(MouseEvent event) {
        doSend();
        event.consume();
    }

    private void doSend() {
        String text = chatInputArea.getText();
        String target = receiverChoiceBox.getValue();
        log.warn("Send text[{}] to {}", text, target);
        if (target.equals(ALL)) {
            target = SessionManager.getInstance().getCurrentMeeting().getUuid();
        }
        new ChatSenderService((VBox) chatBoxScrollPane.getContent(), target, text).start();
        chatInputArea.clear();
        sendMessageLabel.requestFocus();
    }

    @FXML
    public void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            doSend();
        }
        event.consume();
    }


}
