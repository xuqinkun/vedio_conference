package controller;

import common.bean.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.model.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView portrait;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getName());
            if (currentUser.getPortrait() != null) {
                portrait.setImage(new Image(currentUser.getPortrait()));
            }
        }
    }

    @FXML
    public void createMeetingClick(MouseEvent event) {
        try {
            Stage profileStage = (Stage) usernameLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/CreateMeeting.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initOwner(profileStage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Stage joinMeetingStage;

    @FXML
    public void joinMeeting(MouseEvent event) throws IOException {
        if (joinMeetingStage == null) {
            Stage profileStage = (Stage) usernameLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/JoinMeeting.fxml"));
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
    }
}
