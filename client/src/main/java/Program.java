import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.model.SessionManager;
import service.schedule.layout.LayoutInitTask;
import util.DeviceManager;

import java.util.List;

public class Program extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        /* Initialize Devices */
        new Thread(DeviceManager::initGrabber).start();
        new Thread(DeviceManager::initAudioPlayer).start();
        new Thread(DeviceManager::initAudioTarget).start();
//        new Thread(new LayoutInitTask()).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> params = getParameters().getRaw();
        boolean debugMode = params.size() > 0 && params.get(0).equalsIgnoreCase("-d");
        primaryStage.setTitle("Meeting");
        Parent root;
        SessionManager.getInstance().setDebugMode(debugMode);
        if (debugMode) {
            root = FXMLLoader.load(getClass().getResource("/fxml/MeetingRoom.fxml"));
        } else {
            root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        }
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
