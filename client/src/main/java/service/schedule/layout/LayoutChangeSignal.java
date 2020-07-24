package service.schedule.layout;

import common.bean.OperationType;
import javafx.scene.layout.Pane;

public class LayoutChangeSignal {
    private final OperationType op;
    private final String controlID;
    private final Pane pane;

    public LayoutChangeSignal(OperationType op, String controlID, Pane pane) {
        this.op = op;
        this.controlID = controlID;
        this.pane = pane;
    }

    public OperationType getOp() {
        return op;
    }

    public String getControlID() {
        return controlID;
    }

    public Pane getPane() {
        return pane;
    }
}
