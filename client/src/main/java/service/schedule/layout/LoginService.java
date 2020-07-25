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
import util.LayoutUtil;

import java.io.IOException;

public class LoginService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private User user;

    private VBox mainLayout;

    public LoginService(User user, Button loginBtn, VBox mainLayout, Label loginMessageLabel) {
        this.user = user;
        this.mainLayout = mainLayout;
        initListener(loginBtn, loginMessageLabel);
    }

    private void initListener(Button loginBtn, Label loginMessageLabel) {
        valueProperty().addListener((observable, oldValue, response) -> {
            if (response.getResult() == ResultCode.OK) {
                loginBtn.setDisable(true);
                gotoProfile();
            } else {
                loginMessageLabel.setStyle("-fx-text-fill: red");
                loginMessageLabel.setText(response.getMessage());
                loginBtn.setDisable(false);
            }
        });
        exceptionProperty().addListener((observable, oldValue, throwable) -> {
            log.error(throwable.getMessage());
        });
    }

    private void gotoProfile() {
        Stage stage = (Stage) mainLayout.getScene().getWindow();
        stage.close();
        Parent root = LayoutUtil.loadFXML("/fxml/Profile.fxml");
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
