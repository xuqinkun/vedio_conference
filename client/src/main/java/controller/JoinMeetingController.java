package controller;

import common.bean.User;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.layout.JoinMeetingTask;
import util.InputChecker;

import java.net.URL;
import java.util.ResourceBundle;

public class JoinMeetingController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    @FXML
    private Button joinMeetingBtn;

    @FXML
    private TextField meetingIDInput;

    @FXML
    private TextField userNameInput;

    @FXML
    private CheckBox audioCheckBox;

    @FXML
    private CheckBox cameraCheckBox;

    @FXML
    private Label joinMeetingMessageLabel;

    @FXML
    private TextField meetingPasswordInput;

    @FXML
    private Parent joinMeetingLayout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (meetingIDInput != null)
            meetingIDInput.textProperty().addListener((observable, oldValue, newValue) -> inputCheck());
        if (userNameInput != null)
            userNameInput.textProperty().addListener((observable, oldValue, newValue) -> inputCheck());
        if (meetingPasswordInput != null)
            meetingPasswordInput.textProperty().addListener((observable, oldValue, newValue) -> inputCheck());
        User user = SessionManager.getInstance().getCurrentUser();
        String username = user == null ? "test" : user.getName();
        userNameInput.setText(username);
    }

    @FXML
    public void joinMeeting(ActionEvent event) {
        joinMeetingBtn.setDisable(true);
        joinMeetingMessageLabel.setText("Waiting for server's response...");
        joinMeetingMessageLabel.setStyle("-fx-text-fill: #0055ff");
        if (validateInput()) {
//            boolean openAudio = audioCheckBox.isSelected();
//            boolean openCamera = cameraCheckBox.isSelected();
            String userName = userNameInput.getText();
            String meetingID = meetingIDInput.getText();
            String meetingPassword = meetingPasswordInput.getText();
            new Thread(new JoinMeetingTask(meetingID, userName, meetingPassword, joinMeetingBtn, joinMeetingMessageLabel)).start();
        } else {
            joinMeetingBtn.setDisable(false);
        }
        event.consume();
    }

    private boolean validateInput() {
        String userName = userNameInput.getText();
        String meetingPassword = meetingPasswordInput.getText();
        String meetingNumber = meetingIDInput.getText();
        if (StringUtils.isEmpty(meetingNumber) || StringUtils.isEmpty(userName) || StringUtils.isEmpty(meetingPassword))
            return false;
        else return true;
    }

    public void inputCheck() {
        if (InputChecker.validInput(userNameInput.getText()) &&
                InputChecker.validInput(meetingPasswordInput.getText()) &&
                InputChecker.validInput(meetingIDInput.getText()))
            joinMeetingBtn.setDisable(false);
        else
            joinMeetingBtn.setDisable(true);
    }
}
