package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button joinMeetingBtn;

    @FXML
    private VBox mainLayout;

    @FXML
    private VBox homeLayout;

    @FXML
    private VBox loginLayout;

    @FXML
    private VBox registerLayout;

    private Stage stage;

    private Parent root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideControl(loginLayout);
        hideControl(registerLayout);
        displayControl(homeLayout);
    }

    @FXML
    public void joinMeeting(ActionEvent event) {
        try {
            if (stage == null) {
                root = FXMLLoader.load(
                        getClass().getResource("/fxml/JoinMeetings.fxml"));
                stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Join Meeting");
                stage.setResizable(false);
                stage.initStyle(StageStyle.UNDECORATED);
            }
            stage.setX(mainLayout.getScene().getWindow().getX() + mainLayout.getWidth() + 10);
            stage.setY(mainLayout.getScene().getWindow().getY());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loginOrRegister(ActionEvent event) {
        hideControl(homeLayout);
        hideControl(registerLayout);
        displayControl(loginLayout);
    }

    @FXML
    public void backToHome(MouseEvent event) {
        hideControl(loginLayout);
        hideControl(registerLayout);
        displayControl(homeLayout);
    }

    @FXML
    public void backToLogin(MouseEvent event) {
        hideControl(registerLayout);
        displayControl(loginLayout);
    }

    @FXML
    public void goToRegister(MouseEvent event) {
        hideControl(loginLayout);
        displayControl(registerLayout);
    }

    @FXML
    public void login(ActionEvent event) {
        System.out.println("login");
    }

    @FXML
    public void register(ActionEvent event) {
        System.out.println("register");
    }

    private void hideControl(Parent node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    private void displayControl(Parent node) {
        node.setVisible(true);
        node.setManaged(true);
    }
}
