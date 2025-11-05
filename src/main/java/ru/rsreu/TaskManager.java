package ru.rsreu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {
    private final Map<Integer, Thread> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final Semaphore taskSemaphore;

    public TaskManager() {
        this(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
    }

    public TaskManager(int maxConcurrentTasks) {
        this.taskSemaphore = new Semaphore(Math.max(1, maxConcurrentTasks));
    }

    public int startParallelTask(int totalPoints) {
        int taskId = idGenerator.getAndIncrement();
        try {
            taskSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Не удалось запустить задачу: поток был прерван", e);
        }

        ParallelMonteCarloTask task = new ParallelMonteCarloTask(totalPoints, 4, taskId, taskSemaphore);
        Thread thread = new Thread(task);

        tasks.put(taskId, thread);
        thread.start();
        System.out.println("Параллельная задача #" + taskId + " запущена.");
        return taskId;
    }
}
