package ua.procamp.taskpool.queue;

import lombok.SneakyThrows;

import java.util.LinkedList;

public class TaskQueue {

    private LinkedList<Runnable> queue = new LinkedList<>();

    private int limit;

    public TaskQueue(int limit) {
        this.limit = limit;
    }

    public synchronized void add(Runnable item) {
        if (this.queue.size() == this.limit) {
            throw new TaskQueueIsFullException();
        }
        if (this.queue.size() == 0) {
            notifyAll();
        }
        this.queue.add(item);
    }

    @SneakyThrows
    public synchronized Runnable poll() {
        if (this.queue.size() == 0) {
            wait();
        }
        return this.queue.poll();
    }

    public synchronized boolean isEmpty() {
        return this.queue.isEmpty();
    }

}

