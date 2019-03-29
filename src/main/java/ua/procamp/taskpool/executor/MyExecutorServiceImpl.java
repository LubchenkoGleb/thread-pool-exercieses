package ua.procamp.taskpool.executor;

import lombok.SneakyThrows;
import ua.procamp.taskpool.queue.TaskQueue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class MyExecutorServiceImpl implements MyExecutorService {

    private final TaskQueue taskQueue;

    private final List<Thread> threadPoll;

    private volatile boolean interrupted;

    public MyExecutorServiceImpl(int poolSize, int workQueueSize) {
        this.taskQueue = new TaskQueue(workQueueSize);
        this.threadPoll = initThreads(poolSize);
    }

    private List<Thread> initThreads(int poolSize) {

        return IntStream.range(0, poolSize)
                .mapToObj(i -> new Thread(() -> {
                    while (!taskQueue.isEmpty() || !interrupted) {
//                        System.out.println(
//                                LocalDateTime.now() + " " + Thread.currentThread().getName() + " peeked up a task");
                        Optional.ofNullable(taskQueue.poll()).ifPresent(Runnable::run);
                    }
                }))
                .peek(Thread::start)
                .collect(toList());
    }

    @Override
    public void execute(Runnable command) {
        if (this.interrupted) {
            throw new ExecutorTerminatedException("Executor is terminated");
        }
        this.taskQueue.add(command);
    }

    @Override
    public void shutdownNow() {
        this.interrupted = true;
    }

    @Override
    public boolean isTerminated() {
        return threadPoll.stream()
//                .peek(t -> System.out.println(t.getName() + " " + t.getState()))
                .noneMatch(Thread::isAlive);
    }

    @Override
    @SneakyThrows
    public void awaitTermination(int time, TimeUnit timeUnit) {

        long millis = timeUnit.toMillis(time);
        Thread.sleep(millis);

        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }

        if (!isTerminated()) {
            throw new InterruptedException("Unable to stop pool");
        }
    }
}
