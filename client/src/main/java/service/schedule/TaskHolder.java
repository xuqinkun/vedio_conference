package service.schedule;

public class TaskHolder<T> {
    private T task;
    private volatile boolean started;
    private boolean submitted;

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

    public void submit() {
        TaskStarter.submit(this);
        submitted = true;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
