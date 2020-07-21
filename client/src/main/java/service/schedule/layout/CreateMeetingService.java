package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import controller.CreateMeetingController;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;
import util.Helper;

import java.io.IOException;
import java.util.Date;

public class CreateMeetingService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(CreateMeetingController.class);

    private Stage currentStage;

    private String password;

    private String meetingType;

    public CreateMeetingService(Stage currentStage, String password, String meetingType, Button createBtn) {
        this.currentStage = currentStage;
        this.password = password;
        this.meetingType = meetingType;

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getResult() == ResultCode.OK) {
                try {
                    drawMeetingRoom();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                createBtn.setDisable(false);
            }
        });
    }

    @Override
    protected Task<HttpResult<String>> createTask() {
        return new Task<HttpResult<String>>() {
            @Override
            protected HttpResult<String> call() {
                Date date = new Date();
                String time = Helper.dateFormat(date);
                Meeting meeting = new Meeting(Helper.getUuid(), password, meetingType, time, time, true, false);
                String username = SessionManager.getInstance().getCurrentUser().getName();
                meeting.setOwner(username);
                HttpResult<String> response = HttpClientUtil.getInstance().doPost(UrlMap.getCreateMeetingUrl(), meeting);
                if (response != null && response.getResult() == ResultCode.OK) {
                    log.warn("Create meeting[{}] succeed", meeting);
                    SessionManager.getInstance().setCurrentMeeting(meeting);
                } else {
                    log.error("Create meeting[{}] failed", meeting);
                    return new HttpResult<>(ResultCode.ERROR, "No response from server. Maybe server is down.");
                }
                return response;
            }
        };
    }

    private void drawMeetingRoom() throws java.io.IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/MeetingRoom.fxml"));
        Stage roomStage = new Stage();
        roomStage.setScene(new Scene(root));
        currentStage.close();
        ((Stage) currentStage.getOwner()).close();
        Button leaveMeetingBtn = (Button) roomStage.getScene().getRoot().lookup("#leaveMeetingBtn");
        roomStage.setOnCloseRequest(event -> {
            log.warn("setOnCloseRequest");
            leaveMeetingBtn.fire();
            event.consume();
        });
        roomStage.show();
    }
}
