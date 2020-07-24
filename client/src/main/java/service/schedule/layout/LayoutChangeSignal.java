package service.schedule.layout;

import common.bean.OperationType;
import javafx.scene.layout.Pane;

public class LayoutChangeSignal {
    private final OperationType op;
    private final String userName;
    private final Pane pane;

    public LayoutChangeSignal(OperationType op, String userName, Pane pane) {
        this.op = op;
        this.userName = userName;
        this.pane = pane;
    }

    public OperationType getOp() {
        return op;
    }

    public String getUserName() {
        return userName;
    }

    public Pane getPane() {
        return pane;
    }
}
