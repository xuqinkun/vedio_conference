package util;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.ChatMessage;

import java.io.IOException;

public class LayoutUtil {

    private static final Logger log = LoggerFactory.getLogger(LayoutUtil.class);

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

    public static void showDialog(Stage mainStage, String content, EventHandler<MouseEvent> handler) {
        Parent dialog = LayoutUtil.loadFXML("/fxml/Dialog.fxml");
        Label titleLabel = (Label) dialog.lookup("#titleLabel");
        Label contentLabel = (Label) dialog.lookup("#contentLabel");
        Button cancelBtn = (Button) dialog.lookup("#cancelBtn");
        Button confirmBtn = (Button) dialog.lookup("#confirmBtn");
        titleLabel.setText("Leave Meeting");
        contentLabel.setText(content);
        Stage dialogStage = new Stage();
        dialogStage.setScene(new Scene(dialog));
        dialogStage.initOwner(mainStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        confirmBtn.setOnMouseClicked(event -> {
            dialogStage.close();
            handler.handle(event);
        });
        cancelBtn.setOnMouseClicked((event) -> {
            dialogStage.close();
        });
        dialogStage.show();
    }

    public static Parent loadFXML(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader();
        try {
            return loader.load(LayoutUtil.class.getResourceAsStream(fxmlPath));
        } catch (IOException e) {
            log.error("Load fxml[{}] failed", fxmlPath);
            log.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
