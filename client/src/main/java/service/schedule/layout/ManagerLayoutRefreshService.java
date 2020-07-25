package service.schedule.layout;

import common.bean.Meeting;
import common.bean.OperationType;
import controller.ManagerViewController;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import util.SystemUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static common.bean.OperationType.MANAGER_ADD;
import static common.bean.OperationType.MANAGER_REMOVE;

public class ManagerLayoutRefreshService extends ScheduledService<LayoutChangeSignal> {

    private static final Logger log = LoggerFactory.getLogger(ManagerViewController.class);
    private final SessionManager sessionManager = SessionManager.getInstance();

    private VBox managerBox;

    public ManagerLayoutRefreshService(VBox managerBox) {
        this.managerBox = managerBox;

        valueProperty().addListener((observable, oldValue, layoutChangeSignal) -> {
            if (layoutChangeSignal != null) {
                OperationType op = layoutChangeSignal.getOp();
                String paneId = layoutChangeSignal.getUserName();
                if (op == MANAGER_ADD && managerBox.lookup("#" + paneId) == null) {
                    log.warn("Add manager[{}] to layout", paneId);
                    managerBox.getChildren().add(layoutChangeSignal.getPane());
                } else if (op == MANAGER_REMOVE && managerBox.lookup("#" + paneId) != null) {
                    Node target = managerBox.lookup("#" + paneId);
                    if (target != null) {
                        log.warn("Remove manager[{}] from layout", paneId);
                        managerBox.getChildren().remove(target);
                    }
                }
            }
        });
    }

    @Override
    protected Task<LayoutChangeSignal> createTask() {
        return new Task<LayoutChangeSignal>() {

            @Override
            protected LayoutChangeSignal call() throws Exception {
                Set<String> managers = sessionManager.getManagers();
                managerAddScan(managers);
                Thread.sleep(1000);
                managerRemoveScan();
                return null;
            }

            private void managerRemoveScan() {
                List<Node> children = managerBox.getChildren();
                for (Node child : children) {
                    String controlId = child.getId();
                    if (!sessionManager.getManagers().contains(controlId)) {
                        updateValue(new LayoutChangeSignal(MANAGER_REMOVE, controlId, null));
                    }
                }
            }

            private void managerAddScan(Set<String> managers) {
                List<Node> children = managerBox.getChildren();
                Set<String> nodeIds = children.stream().map(Node::getId).collect(Collectors.toSet());

                String host = sessionManager.getCurrentMeeting().getHost();
                if (!nodeIds.contains(host)) {
                    updateValue(new LayoutChangeSignal(MANAGER_ADD, host, createManagerItem(host)));
                }
                for (String userName : managers) {
                    if (nodeIds.contains(userName))
                        continue;
                    AnchorPane ap = createManagerItem(userName);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateValue(new LayoutChangeSignal(MANAGER_ADD, userName, ap));
                }
            }

            private AnchorPane createManagerItem(String userName) {
                AnchorPane ap = new AnchorPane();
                ap.setStyle("-fx-border-color: #dadada");
                ap.setId(userName);
                ap.setPadding(new Insets(2));

                Label label;
                if (sessionManager.isMeetingHost(userName)) {
                    label = new Label(userName + " (Host)");
                } else {
                    label = new Label(userName + " (Manager)");
                }
                label.setPrefSize(230, 30);

                Button removeBtn = new Button("Remove");
                String normalStyle = "-fx-background-color: white;-fx-border-color:red; -fx-text-fill: red";
                String activeStyle = "-fx-background-color: red;-fx-text-fill: white";
                removeBtn.setStyle(normalStyle);
                removeBtn.setOnMouseEntered(event -> {
                    removeBtn.setStyle(activeStyle);
                });
                removeBtn.setOnMouseExited(event -> {
                    removeBtn.setStyle(normalStyle);
                });
                removeBtn.setOnAction(event -> {
                    if (sessionManager.isMeetingHost(userName)) {
                        SystemUtil.showSystemInfo("You can't remove yourself from manager members! " +
                                "If you insist, appoint another one as host.");
                    } else {
                        Meeting meeting = sessionManager.getCurrentMeeting();
                        meeting.getManagers().remove(userName);
                        updateValue(new LayoutChangeSignal(MANAGER_REMOVE, userName, null));
                        new PermissionService(meeting.getUuid(), userName, MANAGER_REMOVE).start();
                    }
                });

                ap.getChildren().addAll(label, removeBtn);

                AnchorPane.setLeftAnchor(label, 5.0);
                AnchorPane.setBottomAnchor(label, 2.0);
                AnchorPane.setTopAnchor(label, 2.0);
                AnchorPane.setRightAnchor(removeBtn, 10.0);
                AnchorPane.setBottomAnchor(removeBtn, 2.0);
                AnchorPane.setTopAnchor(removeBtn, 2.0);
                return ap;
            }
        };

    }
}

