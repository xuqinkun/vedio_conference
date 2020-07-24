package service.schedule.layout;


import common.bean.HttpResult;
import common.bean.OperationType;
import common.bean.PermissionContext;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import util.SystemUtil;

import static common.bean.ResultCode.ERROR;

public class PermissionService extends Service<HttpResult<String>> {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private String meetingId;
    private final String username;
    private OperationType operationType;

    public PermissionService(String meetingId, String username, OperationType operationType) {
        this.meetingId = meetingId;
        this.username = username;
        this.operationType = operationType;

        valueProperty().addListener((observable, oldValue, response) -> {
            if (response.getResult() == ERROR) {
                SystemUtil.showSystemInfo(response.getMessage());
            }
        });
    }

    @Override
    protected Task<HttpResult<String>> createTask() {
        return new Task<HttpResult<String>>() {
            @Override
            protected HttpResult<String> call() throws Exception {
                String url = UrlMap.getPermissionControlUrl();
                PermissionContext context = new PermissionContext(meetingId, username, operationType);
                HttpResult<String> response = HttpClientUtil.getInstance().doPost(url, context);
                if (response == null)
                    response = new HttpResult<>(ERROR, "Request failed. Server is down.");
                log.warn(response.getMessage());
                return response;
            }
        };
    }
}
