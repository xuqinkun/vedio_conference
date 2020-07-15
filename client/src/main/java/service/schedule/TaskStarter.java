package service.schedule;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TaskStarter {
    private static final Logger LOG = LoggerFactory.getLogger(TaskStarter.class);

    public static <T> void submit(TaskHolder<T> holder) {
        if (holder == null || holder.getTask() == null) {
            LOG.warn("Null task!");
            return;
        }
        T task = holder.getTask();
        LOG.warn("Submit task: {}", task);
        try {
            Method startMethod = task.getClass().getMethod("start");
            if (startMethod != null) {
                LOG.warn("Start task[{}]", task.getClass().getSimpleName());
                new Thread(() -> {
                    try {
                        startMethod.invoke(task);
                        holder.setStarted(true);
                        LOG.warn("Task[{}] started", task.getClass().getSimpleName());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException {
        FrameGrabber grabber = FrameGrabber.createDefault(0);
        TaskHolder<FrameGrabber> holder = new TaskHolder<>(grabber);
        submit(holder);
        while (!holder.isStarted()) {
            Thread.sleep(100);
        }
        System.out.println("started:" + System.currentTimeMillis());
        Frame frame = grabber.grab();
        System.out.println(frame.data);
    }
}
