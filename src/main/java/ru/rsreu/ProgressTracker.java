package ru.rsreu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgressTracker {
    private final Map<Integer, Double> progressMap = new ConcurrentHashMap<>();
    private final int totalWorkers;

    public ProgressTracker(int totalWorkers) {
        this.totalWorkers = totalWorkers;
    }

    public void update(int workerId, double progress) {
        progressMap.put(workerId, progress);
        double totalProgress = progressMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum() / totalWorkers * 100.0;

        System.out.printf("Общий прогресс: %.2f%%%n", totalProgress);
    }
}
