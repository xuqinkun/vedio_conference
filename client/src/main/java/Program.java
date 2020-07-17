import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Program extends Application {

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Meeting");
//        Parent root = FXMLLoader.load(
//                getClass().getResource("/fxml/main.fxml"));
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/MeetingRoom.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
