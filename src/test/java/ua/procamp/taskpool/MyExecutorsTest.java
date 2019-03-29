package ua.procamp.taskpool;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import ua.procamp.taskpool.executor.ExecutorTerminatedException;
import ua.procamp.taskpool.executor.MyExecutorService;
import ua.procamp.taskpool.queue.TaskQueueIsFullException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyExecutorsTest {

    @Test
    @SneakyThrows
    void testProcessMoreTaskThanQueueCapacity() {

        int chunksAmount = 5, workQueueSize = 10;

        List<Integer> actual = Collections.synchronizedList(new ArrayList<>());
        MyExecutorService myExecutorService = MyExecutors.newFixedThreadPool(4, workQueueSize);

        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < chunksAmount; i++) {
            expected.addAll(IntStream.range(0, workQueueSize).peek(j ->
                    myExecutorService.execute(() -> actual.add(j)))
                    .boxed().collect(toList()));
            Thread.sleep(1000);
        }

        myExecutorService.shutdownNow();
        myExecutorService.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    @SneakyThrows
    void testTaskProcessingCorrectCase() {

        int workQueueSize = 10;
        List<Integer> actual = Collections.synchronizedList(new ArrayList<>());
        MyExecutorService myExecutorService = MyExecutors.newFixedThreadPool(4, workQueueSize);

        List<Integer> expected = IntStream.range(0, workQueueSize).peek(i ->
                myExecutorService.execute(() -> actual.add(i)))
                .boxed().collect(toList());

        myExecutorService.shutdownNow();
        myExecutorService.awaitTermination(3, TimeUnit.SECONDS);

        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    void testExecutionOrder() throws InterruptedException {

        MyExecutorService myExecutorService = MyExecutors.newFixedThreadPool(4, 4);

        List<String> result = new LinkedList<>();

        myExecutorService.execute(() -> result.add("thread1"));
        Thread.sleep(1000);

        myExecutorService.execute(() -> result.add("thread2"));

        Thread.sleep(1000);
        myExecutorService.execute(() -> result.add("thread3"));

        myExecutorService.shutdownNow();
        myExecutorService.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(asList("thread1", "thread2", "thread3"), result);
    }

    @Test
    void testWhenQueueIsFull() {

        MyExecutorService myExecutorService = MyExecutors.newFixedThreadPool(2, 5);

        assertThrows(TaskQueueIsFullException.class, () ->
                IntStream.range(0, 10).forEach(i -> myExecutorService.execute(() -> System.out.println(i)))
        );
    }

    @Test
    void testAddToTerminatedPool() {
        MyExecutorService myExecutorService = MyExecutors.newFixedThreadPool(2, 5);
        myExecutorService.shutdownNow();

        assertThrows(ExecutorTerminatedException.class, () ->
                myExecutorService.execute(() -> System.out.println("Oh no!")));

    }
}