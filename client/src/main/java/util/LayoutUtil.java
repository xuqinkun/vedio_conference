package util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import service.model.ChatMessage;

public class LayoutUtil {

    public static VBox drawChatItemBox(double width, ChatMessage chatMessage) {
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPrefWidth(width);

        int labelHeight = 30;

        Label timeLabel = decorate(width, labelHeight, chatMessage.getDate());
        timeLabel.setTextAlignment(TextAlignment.CENTER);
        timeLabel.setAlignment(Pos.CENTER);

        Label usernameLabel = decorate(width, labelHeight, chatMessage.getUserName());
        usernameLabel.setStyle("-fx-text-fill: green;-fx-font-size: 14");

        Label msgLabel = decorate(width, labelHeight, chatMessage.getContent());

        VBox.setMargin(usernameLabel, new Insets(0, 0, 0, 10));
        VBox.setMargin(msgLabel, new Insets(0, 0, 0, 10));

        vBox.getChildren().addAll(timeLabel, usernameLabel, msgLabel);
        return vBox;
    }


    private static Label decorate(double width, int height, String str) {
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
