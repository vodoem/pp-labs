package ru.rsreu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class ProgressTracker {
    private final Map<Integer, Double> progressMap = new ConcurrentHashMap<>();
    private final int totalWorkers;
    private final Lock lock;

    public ProgressTracker(int totalWorkers, Lock lock) {
        this.totalWorkers = totalWorkers;
        this.lock = lock;
    }

    public void update(int workerId, double progress) {
        lock.lock();
        try {
            progressMap.put(workerId, progress);
            double totalProgress = progressMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum() / totalWorkers * 100.0;

            System.out.printf("Общий прогресс: %.2f%%%n", totalProgress);
        } finally {
            lock.unlock();
        }
    }
}
