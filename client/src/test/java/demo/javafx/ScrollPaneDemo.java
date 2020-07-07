package demo.javafx;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ScrollPaneDemo extends Application {
    @Override
    public void start(Stage stage) {
        stage.setWidth(500);
        stage.setHeight(500);
        Scene scene = new Scene(new Group());

        VBox root = new VBox();


        TextArea textArea = new TextArea();

        ScrollPane scrollPane = new ScrollPane();

        scrollPane.setContent(textArea);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);



        root.getChildren().addAll(scrollPane);
        scene.setRoot(root);

        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}


