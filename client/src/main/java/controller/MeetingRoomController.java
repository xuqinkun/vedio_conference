package controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MeetingRoomController implements Initializable {
    @FXML
    private Pane rootLayout;

    @FXML
    private Pane userListLayout;

    @FXML
    private Pane chatLayout;

    @FXML
    private Pane titleBar;

    @FXML
    private RadioButton openChatBtn;

    @FXML
    private ChoiceBox<String> receiverChoiceBox;

    private double lastX;

    private double lastY;

    private double oldStageX;

    private double oldStageY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideControl(userListLayout);
        hideControl(chatLayout);
        rootLayout.setPrefWidth(rootLayout.getPrefWidth() - userListLayout.getPrefWidth() - chatLayout.getPrefWidth());
        titleBar.prefWidthProperty().bind(rootLayout.widthProperty());
        receiverChoiceBox.getItems().add("All");
        receiverChoiceBox.getSelectionModel().selectFirst();
    }

    private void hideControl(Parent node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    private void displayControl(Parent node) {
        node.setVisible(true);
        node.setManaged(true);
    }

    @FXML
    public void openOrCloseChat() {
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        int addWidth = 8;
        if (openChatBtn.isSelected()) {
            double newSize = rootLayout.getWidth() + chatLayout.getPrefWidth();
            stage.setWidth(newSize + addWidth);
            displayControl(chatLayout);
        } else {
            hideControl(chatLayout);
            stage.setWidth(rootLayout.getWidth() - chatLayout.getWidth() - addWidth);
        }
    }

    @FXML
    public void mouseDragEnter(MouseEvent event) {
        lastX = event.getScreenX();
        lastY = event.getScreenY();
    }

//    @FXML
//    public void mouseClick(MouseEvent event) {
//        offsetX = event.getScreenX();
//        offsetY = event.getScreenY();
//    }

    @FXML
    public void mousePress(MouseEvent event) {
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        oldStageX = stage.getX();
        oldStageY = stage.getY();
        lastX = event.getScreenX();
        lastY = event.getScreenY();
    }

    @FXML
    public void mouseDrag(MouseEvent event) {
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        stage.setX(oldStageX + event.getScreenX() - lastX);
        stage.setY(oldStageY + event.getScreenY() - lastY);
    }
}
