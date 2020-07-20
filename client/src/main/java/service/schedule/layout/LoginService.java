package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.ResultCode;
import common.bean.User;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;

import java.io.IOException;

public class LoginService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private User user;

    private Button loginBtn;

    private VBox mainLayout;

    public LoginService(User user, Button loginBtn, VBox mainLayout, Label loginMessageLabel) {
        this.user = user;
        this.loginBtn = loginBtn;
        this.mainLayout = mainLayout;

        valueProperty().addListener((observable, oldValue, response) -> {
            if (response.getResult() == ResultCode.OK) {
                try {
                    gotoProfile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                loginMessageLabel.setStyle("-fx-text-fill: red");
                loginMessageLabel.setText(response.getMessage());
            }
        });
    }

    private void gotoProfile() throws IOException {
        Stage stage = (Stage) mainLayout.getScene().getWindow();
        stage.close();
        loginBtn.setDisable(true);
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
            protected HttpResult<String> call() {
                HttpResult<String> result = HttpClientUtil.getInstance().doPost(UrlMap.getLoginUrl(), user);
                if (result == null) {
                    String errMsg = "Login request failed! Server is down!";
                    log.error(errMsg);
                    return new HttpResult<>(ResultCode.ERROR, errMsg);
                } else {
                    log.warn(result.toString());
                    if (result.getResult() == ResultCode.OK) {
                        SessionManager.getInstance().setCurrentUser(user);
                    }
                }
                return result;
            }
        };
    }
}
