package util;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import service.model.ChatMessage;
import service.model.SessionManager;

import java.io.IOException;

public class LayoutUtil {

    public static VBox drawChatItemBox(double width, ChatMessage chatMessage, boolean isLocalMessage) {
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPrefWidth(width);

        int labelHeight = 30;

        Label timeLabel = decorate(width, labelHeight, chatMessage.getDate());
        timeLabel.setTextAlignment(TextAlignment.CENTER);
        timeLabel.setAlignment(Pos.CENTER);

        String suffix = "";
        if (!isLocalMessage) {
            suffix = chatMessage.isPersonal() ? "(Personal)" : "(All)";
        }

        Label usernameLabel = decorate(width, labelHeight, chatMessage.getSenderName() + " " + suffix);
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
