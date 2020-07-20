package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label contentLabel;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button confirmBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
