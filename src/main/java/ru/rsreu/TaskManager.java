package ru.rsreu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {
    private final Map<Integer, Thread> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public int startParallelTask(int totalPoints) {
        int taskId = idGenerator.getAndIncrement();
        ParallelMonteCarloTask task = new ParallelMonteCarloTask(totalPoints, 4, taskId);
        Thread thread = new Thread(task);

        tasks.put(taskId, thread);
        thread.start();
        System.out.println("Параллельная задача #" + taskId + " запущена.");
        return taskId;
    }
}
