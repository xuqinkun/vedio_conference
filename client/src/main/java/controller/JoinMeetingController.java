package controller;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import common.bean.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;
import util.InputChecker;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    private TextField meetingPasswordInput;

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
        if (validateInput()) {
            boolean openAudio = audioCheckBox.isSelected();
            boolean openCamera = cameraCheckBox.isSelected();
            String userName = userNameInput.getText();
            String meetingID = meetingIDInput.getText();
            String meetingPassword = meetingPasswordInput.getText();
            User user = SessionManager.getInstance().getCurrentUser();
            user.setName(userName);
            Meeting meeting = new Meeting();
            meeting.setUuid(meetingID);
            meeting.setPassword(meetingPassword);
            SessionManager.getInstance().setCurrentMeeting(meeting);
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("meeting", meeting);
            HttpResult<String> response = HttpClientUtil.getInstance().doPost(UrlMap.getJoinMeetingUrl(), data);
            if (response.getResult() == ResultCode.OK) {
                try {
                    displayMeetingRoom();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }

            } else{
                log.warn("Join meeting failed.{}", response.getMessage());
            }
        }
    }

    private void displayMeetingRoom() throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/MeetingRoom.fxml"));
        Stage roomStage = new Stage();
        roomStage.setScene(new Scene(root));
        Stage joinStage = (Stage) joinMeetingBtn.getScene().getWindow();
        Stage mainStage = (Stage) joinStage.getOwner();

        mainStage.close();
        joinStage.close();
        roomStage.show();
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
