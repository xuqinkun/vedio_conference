package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import service.model.SessionManager;
import service.schedule.layout.ManagerLayoutRefreshService;

import java.net.URL;
import java.util.ResourceBundle;

public class ManagerViewController implements Initializable {

    @FXML
    private VBox managerBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ManagerLayoutRefreshService refreshService = new ManagerLayoutRefreshService(managerBox);
        refreshService.setPeriod(Duration.seconds(1));
        refreshService.start();
        SessionManager.getInstance().setManagerLayoutRefreshService(refreshService);
    }
}