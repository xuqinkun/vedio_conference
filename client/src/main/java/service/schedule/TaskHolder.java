package service.schedule;

public class TaskHolder<T> {
    private T task;
    private volatile boolean started;

    public TaskHolder(T task) {
        this.task = task;
        started = false;
    }

    public T getTask() {
        return task;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
