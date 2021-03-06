package controller;

import common.bean.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.layout.LogoutService;
import util.LayoutUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView portrait;

    @FXML
    private Button logoutBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getName());
            if (currentUser.getPortraitSrc() != null) {
                portrait.setImage(new Image(currentUser.getPortraitSrc()));
            }
        }
        logoutBtn.setOnMouseEntered(event -> {
            logoutBtn.setStyle("-fx-text-fill: white;-fx-background-color: red");
        });
        logoutBtn.setOnMouseExited(event -> {
            logoutBtn.setStyle("-fx-text-fill: red;-fx-background-color: white;-fx-border-color: red");
        });
    }

    @FXML
    public void createMeetingClick(MouseEvent event) {
        Stage profileStage = (Stage) usernameLabel.getScene().getWindow();
        Parent root = LayoutUtil.loadFXML("/fxml/CreateMeeting.fxml");
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initOwner(profileStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
        event.consume();
    }

    private Stage joinMeetingStage;

    @FXML
    public void joinMeeting(MouseEvent event) throws IOException {
        if (joinMeetingStage == null) {
            Stage profileStage = (Stage) usernameLabel.getScene().getWindow();
            Parent root = LayoutUtil.loadFXML("/fxml/JoinMeeting.fxml");
            joinMeetingStage = new Stage();
            joinMeetingStage.setResizable(false);
            joinMeetingStage.setScene(new Scene(root));
            joinMeetingStage.setX(profileStage.getScene().getWindow().getX() + profileStage.getWidth() + 10);
            joinMeetingStage.setY(profileStage.getScene().getWindow().getY());
            joinMeetingStage.initOwner(profileStage);
            joinMeetingStage.show();
        }
        else if (joinMeetingStage.isShowing()) {
            joinMeetingStage.hide();
        }
        else {
            joinMeetingStage.show();
        }
        event.consume();
    }


    @FXML
    public void logout(ActionEvent event) {
        logoutBtn.setDisable(true);
        new LogoutService(logoutBtn).start();
        event.consume();
    }
}
