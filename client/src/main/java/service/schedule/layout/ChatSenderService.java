package service.schedule.layout;

import common.bean.Message;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.messaging.MessageSender;
import util.Helper;

import static common.bean.OperationType.CHAT_MESSAGE;

public class ChatSenderService extends Service<VBox> {

    private static final Logger log = LoggerFactory.getLogger(ChatSenderService.class);

    private ScrollPane chatBoxScrollPane;

    private String topic;

    private String text;

    public ChatSenderService(ScrollPane chatBoxScrollPane, String topic, String text) {
        this.chatBoxScrollPane = chatBoxScrollPane;
        this.topic = topic;
        this.text = text;

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ((VBox) chatBoxScrollPane.getContent()).getChildren().add(newValue);
            }
        });
    }

    @Override
    protected Task<VBox> createTask() {
        return new Task<VBox>() {
            @Override
            protected VBox call() throws Exception {
                if (!StringUtils.isEmpty(text.trim())) {
                    double width = chatBoxScrollPane.getWidth() - 10;
                    VBox vBox = new VBox();
                    vBox.setSpacing(5);
                    vBox.setPrefWidth(width);

                    int labelHeight = 30;
                    Label timeLabel = decorate(width, labelHeight, Helper.currentDate());
                    timeLabel.setTextAlignment(TextAlignment.CENTER);
                    timeLabel.setAlignment(Pos.CENTER);
                    Label usernameLabel = decorate(width, labelHeight, "usernameLabel");
                    Label msgLabel = decorate(width, labelHeight, text);
                    usernameLabel.setStyle("-fx-text-fill: green;");
                    VBox.setMargin(usernameLabel, new Insets(0, 0, 0, 5));
                    VBox.setMargin(msgLabel, new Insets(0, 0, 0, 5));

                    MessageSender.getInstance().send(topic, new Message(CHAT_MESSAGE, text));

                    vBox.getChildren().addAll(timeLabel, usernameLabel, msgLabel);
                    return vBox;
                }
                return null;
            }
        };
    }


    private Label decorate(double width, int height, String str) {
        Label label = new Label(str);
        label.setPadding(new Insets(5));
        label.setPrefSize(width, height);
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setWrapText(true);
        return label;
    }
}
