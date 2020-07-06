package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

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
        meetingNumberInput.textProperty().addListener((observable, oldValue, newValue) -> inputCheck());
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
        String meetingNumber = meetingNumberInput.getText();
        String userName = userNameInput.getText();
        if (StringUtils.isEmpty(meetingNumber) || StringUtils.isEmpty(userName))
        return true;
        else return false;
    }

    public void inputCheck() {
        if (validateInput())
            joinMeetingBtn.setDisable(true);
        else
            joinMeetingBtn.setDisable(false);
    }
}
