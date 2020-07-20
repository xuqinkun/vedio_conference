package controller;

import common.bean.HttpResult;
import common.bean.ResultCode;
import common.bean.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.schedule.layout.LoginService;
import util.InputChecker;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private VBox mainLayout;

    @FXML
    private VBox homeLayout;

    @FXML
    private VBox loginLayout;

    @FXML
    private VBox registerLayout;

    @FXML
    private TextField loginUserName;

    @FXML
    private TextField loginPassword;

    @FXML
    private TextField registerUserName;

    @FXML
    private TextField registerPassword;

    @FXML
    private TextField registerPassword2;

    @FXML
    private TextField registerEmail;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginBtn;

    @FXML
    private Button registerBtn;

    @FXML
    private Label loginMessageLabel;

    private Stage joinStage;

    private Parent root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideControl(loginLayout);
        hideControl(registerLayout);
//        displayControl(homeLayout);
        registerBtn.setDisable(true);
        loginUserName.textProperty().addListener((observable, oldValue, newValue) -> loginCheck());
        loginPassword.textProperty().addListener((observable, oldValue, newValue) -> loginCheck());
        registerUserName.textProperty().addListener((observable, oldValue, newValue) -> registerCheck());
        registerPassword.textProperty().addListener((observable, oldValue, newValue) -> registerCheck());
        registerPassword2.textProperty().addListener((observable, oldValue, newValue) -> registerCheck());
        registerEmail.textProperty().addListener((observable, oldValue, newValue) -> registerCheck());
    }

    @FXML
    public void joinMeeting(ActionEvent event) {
        try {
            if (joinStage == null) {
                root = FXMLLoader.load(
                        getClass().getResource("/fxml/JoinMeeting.fxml"));
                joinStage = new Stage();
                joinStage.setScene(new Scene(root));
                joinStage.setTitle("Join Meeting");
                joinStage.setResizable(false);
//                stage.initStyle(StageStyle.UNDECORATED);
                joinStage.setX(mainLayout.getScene().getWindow().getX() + mainLayout.getWidth() + 10);
                joinStage.setY(mainLayout.getScene().getWindow().getY());
                Stage mainStage = (Stage) mainLayout.getScene().getWindow();
                joinStage.initOwner(mainStage);
                joinStage.show();
            }
            else if (joinStage.isShowing()) {
                joinStage.hide();
            }
            else {
                joinStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loginOrRegister(ActionEvent event) {
        hideControl(homeLayout);
        hideControl(registerLayout);
        displayControl(loginLayout);
    }

    @FXML
    public void backToHome(MouseEvent event) {
        hideControl(loginLayout);
        hideControl(registerLayout);
        displayControl(homeLayout);
    }

    @FXML
    public void backToLogin(MouseEvent event) {
        hideControl(registerLayout);
        displayControl(loginLayout);
    }

    @FXML
    public void goToRegister(MouseEvent event) {
        hideControl(loginLayout);
        displayControl(registerLayout);
    }

    @FXML
    public void login(ActionEvent event) {
        if (loginCheck()) {
            loginMessageLabel.setStyle("-fx-text-fill: #0055ff");
            loginMessageLabel.setText("Waiting for server's response...");
            loginBtn.setDisable(true);
            String userNameText = loginUserName.getText();
            String password = loginPassword.getText();
            User user = new User(userNameText, password);
            new LoginService(user, loginBtn, mainLayout, loginMessageLabel).start();
        } else {
            loginBtn.setDisable(false);
        }
        event.consume();
    }

    private boolean loginCheck() {
        if (StringUtils.isEmpty(loginUserName.getText()) || StringUtils.isEmpty(loginPassword.getText())) {
            loginBtn.setDisable(true);
            return false;
        } else {
            loginBtn.setDisable(false);
            return true;
        }
    }

    private boolean registerCheck() {
        String username = registerUserName.getText();
        String password = registerPassword.getText();
        String password2 = registerPassword2.getText();
        String email = registerEmail.getText();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(password2) || StringUtils.isEmpty(email)) {
            registerBtn.setDisable(true);
            return false;
        }
        else if (!password.equals(password2)) {
            registerBtn.setDisable(true);
            messageLabel.setText("Two passwords is not identical");
            return false;
        }
        else if (!InputChecker.isValidEmail(email)) {
            registerBtn.setDisable(true);
            messageLabel.setText("Email format is illegal");
            return false;
        }
        else {
            messageLabel.setText("");
            registerBtn.setDisable(false);
        }
        return true;
    }

    @FXML
    public void register(ActionEvent event) {
        System.out.println("register");
        String username = registerUserName.getText();
        String password = registerPassword.getText();
        String email = registerEmail.getText();
        registerBtn.setDisable(true);
        User user = new User(username, password, email);
        HttpResult<String> result = HttpClientUtil.getInstance().doPost(UrlMap.getRegisterUrl(), user);
        log.warn(result.toString());
        if (result.getResult() != ResultCode.OK) {
            registerBtn.setDisable(false);
            messageLabel.setText(result.getMessage());
        } else {
            openDialog();
        }
    }

    private void openDialog() {
        Stage primaryStage = (Stage) registerBtn.getScene().getWindow();
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #ffffff");

        bp.setPrefSize(200, 100);
        Label label = new Label("Register success, login Now!");
        label.setFont(Font.font(14));
        bp.setCenter(label);

        Button confirmBtn = new Button("Confirm");
        Button cancelBtn = new Button("Cancel");

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(5));
        hBox.getChildren().addAll(confirmBtn, cancelBtn);

        bp.setBottom(hBox);

        Stage stage = new Stage();
        stage.setScene(new Scene(bp));
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Register");
        stage.setResizable(false);
        stage.show();

        cancelBtn.setOnAction(event -> {
            stage.close();
            registerBtn.setDisable(false);
        });
        confirmBtn.setOnAction(event -> {
            stage.close();
            backToLogin(null);
        });
    }

    private void hideControl(Parent node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    private void displayControl(Parent node) {
        node.setVisible(true);
        node.setManaged(true);
    }
}
