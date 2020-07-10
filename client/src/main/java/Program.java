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
//        new Thread(new MessageReceiver()).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Meeting");
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/Main.fxml"));
        primaryStage.setScene(new Scene(root));
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
