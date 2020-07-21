package service.schedule.layout;

import common.bean.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;

import java.io.IOException;

public class LeaveMeetingService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(LeaveMeetingService.class);

    private final SessionManager sessionManager = SessionManager.getInstance();

    private Pane mainLayout;

    public LeaveMeetingService(Pane mainLayout) {
        this.mainLayout = mainLayout;
        valueProperty().addListener((observable, oldValue, response) -> {
            if (response.getResult() == ResultCode.OK) {
                try {
                    gotoProfile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // TODO print response
                log.warn(response.getMessage());
            }
        });
    }

    private void gotoProfile() throws IOException {
        Stage stage = (Stage) mainLayout.getScene().getWindow();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stage.close();
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/profile.fxml"));
        Stage profileStage = new Stage();
        profileStage.setScene(new Scene(root));
        profileStage.show();
    }

    @Override
    protected Task<HttpResult<String>> createTask() {
        return new Task<HttpResult<String>>() {
            @Override
            protected HttpResult<String> call() throws Exception {
                // 1. send message
                Meeting meeting = sessionManager.getCurrentMeeting();
                User user = sessionManager.getCurrentUser();
                // 2. Inform server leave meeting
                HttpResult<String> response = HttpClientUtil.getInstance().
                        doPost(UrlMap.getLeaveMeetingUrl(), new MeetingContext(user, meeting));
                // 3. close layout, stop threads
                if (response != null) {
                    log.warn(response.getMessage());
                    if (response.getResult() == ResultCode.OK) {
                        log.warn("Stop meeting[{}]", meeting.getUuid());
                        sessionManager.stopMeeting();
                    }
                    return response;
                }
                return new HttpResult<>(ResultCode.ERROR, "Leave meeting failed. Maybe server is down");
            }
        };
    }
}
