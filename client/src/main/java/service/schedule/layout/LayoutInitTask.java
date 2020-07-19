package service.schedule.layout;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import util.ThreadPoolUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class LayoutInitTask extends Task<Object> {

    private static String fxmlRoot = "fxml";

    private static Map<String, Parent> layoutContainer = new ConcurrentHashMap<>();

    private final ScheduledThreadPoolExecutor exec = ThreadPoolUtil.getScheduledExecutor(5, "LayoutInit");

    @Override
    protected Object call() throws Exception {
        String path = Objects.requireNonNull(LayoutInitTask.class.getClassLoader().getResource(fxmlRoot)).getPath();
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().split("\\.")[0];
                    try {
                        Parent node = FXMLLoader.load(new URL("file:"+file.getAbsolutePath()));
                        layoutContainer.put(name, node);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public Parent getLayout(String name) {
        return layoutContainer.get(name);
    }
}
