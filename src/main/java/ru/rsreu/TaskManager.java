package ru.rsreu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {
    private final Map<Integer, Thread> tasks = new ConcurrentHashMap<>();
    private final Map<Integer, MonteCarloTask> taskRunnables = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public int startNewTask(double epsilon) {
        int taskId = idGenerator.getAndIncrement();
        MonteCarloTask task = new MonteCarloTask(epsilon, taskId);
        Thread thread = new Thread(task);

        tasks.put(taskId, thread);
        taskRunnables.put(taskId, task);

        thread.start();
        System.out.println("Задача #" + taskId + " запущена.");

        return taskId;
    }

    public void stopTask(int taskId) {
        MonteCarloTask task = taskRunnables.get(taskId);
        Thread thread = tasks.get(taskId);
        if (task != null) {
            task.requestStop();
            thread.interrupt();
            System.out.println("Запрошено завершение задачи #" + taskId);
        } else {
            System.out.println("Задача с номером " + taskId + " не найдена.");
        }
    }

    public void awaitTask(int taskId) {
        Thread thread = tasks.get(taskId);
        if (thread != null) {
            try {
                thread.join();
                System.out.println("Задача #" + taskId + " завершена.");
            } catch (InterruptedException e) {
                System.err.println("Ожидание задачи #" + taskId + " было прервано.");
            }
        } else {
            System.out.println("Задача с номером " + taskId + " не найдена.");
        }
    }

    public void shutdownAll() {
        for (int id : taskRunnables.keySet()) {
            stopTask(id);
        }
        for (Thread thread : tasks.values()) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
        }
        System.out.println("Все задачи завершены.");
    }
}
