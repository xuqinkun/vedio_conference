package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import common.bean.User;
import controller.JoinMeetingController;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JoinMeetingService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private String meetingID;
    private String userName;
    private String meetingPassword;
    private Button joinMeetingBtn;

    public JoinMeetingService(String meetingID, String userName, String meetingPassword,
                              Button joinMeetingBtn, Label joinMeetingMessageLabel) {
        this.meetingID = meetingID;
        this.userName = userName;
        this.meetingPassword = meetingPassword;
        this.joinMeetingBtn = joinMeetingBtn;

        valueProperty().addListener((observable, oldValue, response) -> {
            if (response.getResult() == ResultCode.ERROR) {
                joinMeetingBtn.setDisable(false);
                joinMeetingMessageLabel.setStyle("-fx-text-fill: red");
                joinMeetingMessageLabel.setText(response.getMessage());
            } else {
                try {
                    displayMeetingRoom();
                    joinMeetingMessageLabel.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayMeetingRoom() throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/MeetingRoom.fxml"));
        Stage roomStage = new Stage();
        roomStage.setScene(new Scene(root));
        Stage joinStage = (Stage) joinMeetingBtn.getScene().getWindow();
        Stage mainStage = (Stage) joinStage.getOwner();

        Button leaveMeetingBtn = (Button) roomStage.getScene().getRoot().lookup("#leaveMeetingBtn");

        roomStage.setOnCloseRequest(event -> {
            log.warn("setOnCloseRequest");
            leaveMeetingBtn.fire();
            event.consume();
        });

        mainStage.close();
        joinStage.close();
        roomStage.show();
    }

    @Override
    protected Task<HttpResult<String>> createTask() {
        return new Task<HttpResult<String>>() {
            @Override
            protected HttpResult<String> call() {
                User user = SessionManager.getInstance().getCurrentUser();
                user.setName(userName);
                Meeting meeting = new Meeting();
                meeting.setUuid(meetingID);
                meeting.setPassword(meetingPassword);
                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("meeting", meeting);
                HttpResult<String> response = HttpClientUtil.getInstance().doPost(UrlMap.getJoinMeetingUrl(), data);
                if (response != null) {
                    String resultMessage = response.getMessage();
                    if (response.getResult() == ResultCode.OK) {
                        Meeting oldMeeting = JsonUtil.jsonToObject(resultMessage, Meeting.class);
                        SessionManager.getInstance().setCurrentMeeting(oldMeeting);
                    } else {
                        log.error("Join meeting failed.{}", resultMessage);
                    }
                    return response;
                } else {
                    return new HttpResult<>(ResultCode.ERROR, "No response from server. Maybe server is down.");
                }
            }
        };
    }
}
