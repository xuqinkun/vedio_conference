package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.layout.CreateMeetingService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateMeetingController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(CreateMeetingController.class);

    @FXML
    private HBox meetingTypeBox;

    @FXML
    private TextField meetingPassword;

    @FXML
    private Button createBtn;

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
                        ((RadioButton) child).setSelected(false);
                    }
                }
            });
        }
    }

    @FXML
    public void createMeeting(ActionEvent event) {
        log.debug(event.toString());
        Stage stage = (Stage) meetingTypeBox.getScene().getWindow();
        createBtn.setDisable(true);
        new CreateMeetingService(stage, meetingPassword.getText(), meetingType, createBtn).start();
        event.consume();
    }
}
