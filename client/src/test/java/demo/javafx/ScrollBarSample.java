package demo.javafx;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ScrollBarSample extends Application {

    final ScrollBar sc = new ScrollBar();
    final Image[] images = new Image[5];
    final ImageView[] pics = new ImageView[5];
    final VBox vb = new VBox();
    DropShadow shadow = new DropShadow();

    int fillHeight;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 500, 180);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("Scrollbar");
        root.getChildren().addAll(vb, sc);

        shadow.setColor(Color.GREY);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);

        vb.setLayoutX(5);
        vb.setSpacing(10);
        vb.setPrefHeight(170);

        sc.setLayoutX(scene.getWidth() - sc.getWidth());
        sc.setMin(0);
        sc.setOrientation(Orientation.VERTICAL);
        sc.setPrefHeight(180);
        sc.setMax(360);

        sc.setUnitIncrement(10.0);
        sc.setBlockIncrement(5.0);

        for (int i = 0; i < 10; i++) {
//            final Image image = images[i] = new Image(getClass()
//                    .getResourceAsStream("fw" + (i + 1) + ".jpg"));
//            final ImageView pic = pics[i] = new ImageView(images[i]);
//            pic.setEffect(shadow);
            Label test = new Label("test");
            test.setPrefSize(30, 30);
            if (fillHeight + test.getPrefHeight() > vb.getPrefHeight()) {
                vb.setLayoutY(vb.getLayoutY()-test.getPrefHeight());
//                sc.setValue(sc.getValue() - test.getPrefHeight());
            } else {
                fillHeight += test.getPrefHeight();
//                sc.setValue(sc.getValue() + test.getPrefHeight());
            }
            vb.getChildren().add(test);
        }
        sc.setValue(fillHeight);
        sc.valueProperty().addListener((ov, old_val, new_val) -> {
            System.out.println(new_val.doubleValue());
            vb.setLayoutY(-new_val.doubleValue());
            System.out.println(vb.getLayoutY());
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
