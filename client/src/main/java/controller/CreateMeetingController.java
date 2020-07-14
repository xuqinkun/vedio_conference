package controller;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.model.SessionManager;
import util.Helper;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class CreateMeetingController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(CreateMeetingController.class);

    @FXML
    private HBox meetingTypeBox;

    @FXML
    private TextField meetingPassword;

    private String meetingType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Node> children = meetingTypeBox.getChildren();
        for (Node node : children) {
            RadioButton radioButton = (RadioButton) node;
            if (radioButton.isSelected()) {
                meetingType = radioButton.getText();
            }
            radioButton.setOnMouseClicked(event1 -> {
                meetingType = radioButton.getText();
                for (Node child : children) {
                    if (child != node) {
                        ((RadioButton)child).setSelected(false);
                    }
                }
            });
        }
    }

    @FXML
    public void createMeeting(ActionEvent event) throws IOException {
        Stage stage = (Stage) meetingTypeBox.getScene().getWindow();
        Stage parentStage = (Stage)stage.getOwner();
        String password = meetingPassword.getText();
        Date date = new Date();
        Meeting meeting = new Meeting(Helper.getUuid(), password, meetingType, date, date, true, false);
        String username = SessionManager.getInstance().getCurrentUser().getName();
        meeting.setOwner(username);
        HttpResult<String> response = HttpClientUtil.getInstance().doPost(UrlMap.getCreateMeetingUrl(), meeting);
        if (response.getResult() == ResultCode.OK) {
            log.warn("Create meeting[{}] succeed", meeting);
            SessionManager.getInstance().setCurrentMeeting(meeting);
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/MeetingRoom.fxml"));
            Stage roomStage = new Stage();
            roomStage.setScene(new Scene(root));
            stage.close();
            parentStage.close();
            roomStage.show();
        } else{
            log.error("Create meeting[{}] failed", meeting);
        }
    }
}
