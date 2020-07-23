package util;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import service.schedule.layout.AnimationControlTask;


public class SystemUtil {

    public static Stage showSystemInfo(String content) {
        Stage infoStage = new Stage();
        AnchorPane ap = new AnchorPane();
        ap.setMinSize(360, 70);
        ap.setMaxSize(400, 140);
        ap.setPrefSize(400, 140);
        ap.setStyle("-fx-background-color:  #000000");
        ap.setOpacity(0.9);

        Label label = new Label(content);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: #ffffff;-fx-font-family: 'Microsoft YaHei';-fx-font-size: 20");
        label.setPrefSize(400, 70);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);

        ap.getChildren().add(label);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);

        infoStage.initStyle(StageStyle.UNDECORATED);
        infoStage.setAlwaysOnTop(true);
        infoStage.setScene(new Scene(ap));
        infoStage.show();
        new Thread(new AnimationControlTask(ap, infoStage)).start();

        return infoStage;
    }
}
