package controller;

import common.bean.Meeting;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import service.model.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class InvitationController implements Initializable {
    @FXML
    private Label contentLabel;

    @FXML
    private Label infoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Meeting meeting = SessionManager.getInstance().getCurrentMeeting();
        String uuid = meeting == null ? "test" : meeting.getUuid();
        String password = meeting == null ? "test" : meeting.getPassword();
        contentLabel.setText(String.format("Meeting ID: %s\nPassword: %s", uuid, password));
    }

    @FXML
    public void copyInvitation(ActionEvent event) {
        ClipboardContent content = new ClipboardContent();
        content.putString(contentLabel.getText());
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
        infoLabel.setText("Copied to clipboard");
    }
}
