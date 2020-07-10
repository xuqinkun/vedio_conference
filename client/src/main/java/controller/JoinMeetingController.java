package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import util.InputChecker;

import java.net.URL;
import java.util.ResourceBundle;

public class JoinMeetingController implements Initializable {
    @FXML
    private Button joinMeetingBtn;

    @FXML
    private TextField meetingNumberInput;

    @FXML
    private TextField userNameInput;

    @FXML
    private CheckBox audioCheckBox;

    @FXML
    private CheckBox cameraCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (meetingNumberInput != null)
            meetingNumberInput.textProperty().addListener((observable, oldValue, newValue) -> inputCheck());
        if (userNameInput != null)
            userNameInput.textProperty().addListener((observable, oldValue, newValue) -> inputCheck());
    }

    @FXML
    public void joinMeeting(ActionEvent event) {
        if (validateInput()) {
            boolean openAudio = audioCheckBox.isSelected();
            boolean openCamera = cameraCheckBox.isSelected();
        }
    }

    private boolean validateInput() {
        String userName = userNameInput.getText();
        String meetingNumber = meetingNumberInput.getText();
        if (StringUtils.isEmpty(meetingNumber) || StringUtils.isEmpty(userName))
            return true;
        else return false;
    }

    public void inputCheck() {
        if (InputChecker.validInput(userNameInput.getText()) &&
                InputChecker.validInput(meetingNumberInput.getText()))
            joinMeetingBtn.setDisable(false);
        else
            joinMeetingBtn.setDisable(true);
    }
}
