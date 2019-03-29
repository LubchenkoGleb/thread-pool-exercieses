package ua.procamp.taskpool.executor;

public class ExecutorTerminatedException extends RuntimeException {
    public ExecutorTerminatedException(String message) {
        super(message);
    }
}
