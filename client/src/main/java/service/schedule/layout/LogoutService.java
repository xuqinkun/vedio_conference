package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.ResultCode;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;
import util.LayoutUtil;

import static common.bean.ResultCode.OK;

public class LogoutService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(LogoutService.class);

    private Button logoutBtn;

    public LogoutService(Button logoutBtn) {
        this.logoutBtn = logoutBtn;
        initListener(logoutBtn);
    }

    private void initListener(Button loginBtn) {
        valueProperty().addListener((observable, oldValue, response) -> {
            if (response.getResult() == OK) {
                gotoMainLayout();
            }
            loginBtn.setDisable(false);
        });
        exceptionProperty().addListener((observable, oldValue, throwable) -> {
            log.error(throwable.getMessage());
        });
    }

    private void gotoMainLayout() {
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        stage.close();
        Parent root = LayoutUtil.loadFXML("/fxml/Main.fxml");
        Stage mainStage = new Stage();
        mainStage.setScene(new Scene(root));
        mainStage.setOnCloseRequest(event -> {
            log.warn("Exit");
            System.exit(0);
        });
        mainStage.show();
    }

    @Override
    protected Task<HttpResult<String>> createTask() {
        return new Task<HttpResult<String>>() {
            @Override
            protected HttpResult<String> call() {
                String userName = SessionManager.getInstance().getCurrentUser().getName();
                HttpResult<String> result = HttpClientUtil.getInstance().doPost(UrlMap.getLogoutUrl(), userName);
                if (result == null) {
                    String errMsg = "Login request failed! Server is down!";
                    log.error(errMsg);
                    return new HttpResult<>(ResultCode.ERROR, errMsg);
                } else {
                    if (result.getResult() == OK) {
                        SessionManager.getInstance().setCurrentUser(null);
                    }
                    log.warn(result.toString());
                }
                return result;
            }
        };
    }
}
