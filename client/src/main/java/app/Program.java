package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import util.Config;
import util.DeviceManager;
import util.LayoutUtil;

import java.io.IOException;
import java.util.List;

public class Program extends Application {

    private static final Logger log = LoggerFactory.getLogger(Program.class);

    @Override
    public void init() throws Exception {
        super.init();
        /* Initialize Devices */
        new Thread(DeviceManager::initGrabber).start();
        new Thread(DeviceManager::initAudioPlayer).start();
        new Thread(DeviceManager::initAudioTarget).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean debugMode = parseParameters();
        System.out.println(Thread.currentThread());
        primaryStage.setTitle("Meeting");
        Parent root;
        SessionManager.getInstance().setDebugMode(debugMode);

        if (debugMode) {
            root = LayoutUtil.loadFXML("/fxml/MeetingRoom.fxml");
        } else {
            root = LayoutUtil.loadFXML("/fxml/Main.fxml");
        }
        if (root == null) {
            log.error("Load fxml failed");
            System.exit(1);
        }
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        EventHandler<MouseEvent> handler = event -> {
            log.warn("Exit");
            System.exit(0);
        };

        primaryStage.setOnCloseRequest(event -> {
            LayoutUtil.showDialog(primaryStage, "Are you are to exit?", handler);
            event.consume();
        });

        primaryStage.show();
    }

    private boolean parseParameters() {
        Config config = null;

        List<String> params = getParameters().getRaw();
        boolean debugMode = params.contains("-d");
        String propertyKey = "-p";
        String useLocalServerKey = "-l";
        if (params.contains(propertyKey)) {
            int index = params.indexOf(propertyKey);
            if (params.size() == index + 1) {
                printUsage();
                System.exit(1);
            } else {
                String propertyPath = params.get(index + 1);
                config = Config.getInstance(propertyPath);
            }
        } else {
            config = Config.getInstance();
        }
        if (params.contains(useLocalServerKey)) {
            config.setUseLocal(true);
        }
        return debugMode;
    }

    private void printUsage() {
        System.out.println("USAGE: java -jar *.jar [-d(for debug)] [-p property_path]");
    }
}
