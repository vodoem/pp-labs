package ru.rsreu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.rsreu.sync.MonitorReentrantLock;

public class ProgressTracker {
    private final Map<Integer, Double> progressMap = new ConcurrentHashMap<>();
    private final int totalWorkers;
    private final MonitorReentrantLock lock;

    public ProgressTracker(int totalWorkers, MonitorReentrantLock lock) {
        this.totalWorkers = totalWorkers;
        this.lock = lock;
    }

    public void update(int workerId, double progress) {
        boolean locked = false;
        try {
            lock.lock();
            locked = true;
            progressMap.put(workerId, progress);
            double totalProgress = progressMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum() / totalWorkers * 100.0;

            System.out.printf("Общий прогресс: %.2f%%%n", totalProgress);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Поток был прерван при обновлении прогресса", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
