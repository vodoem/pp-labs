package ru.rsreu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelMonteCarloTask implements Runnable {
    private final int totalPoints;
    private final int numThreads;
    private final int taskId;

    public ParallelMonteCarloTask(int totalPoints, int numThreads, int taskId) {
        this.totalPoints = totalPoints;
        this.numThreads = numThreads;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        if (totalPoints <= 0) {
            System.out.printf("Задача #%d завершена без вычислений: количество точек должно быть положительным.%n", taskId);
            return;
        }

        System.out.printf("Задача #%d начала параллельное вычисление.%n", taskId);

        int actualWorkers = Math.min(numThreads, totalPoints);
        ExecutorService executor = Executors.newFixedThreadPool(actualWorkers);
        ProgressTracker tracker = new ProgressTracker(actualWorkers);

        int basePointsPerWorker = totalPoints / actualWorkers;
        int remainder = totalPoints % actualWorkers;

        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < actualWorkers; i++) {
            int workerPoints = basePointsPerWorker + (i < remainder ? 1 : 0);
            futures.add(executor.submit(new PartialMonteCarloWorker(workerPoints, i, tracker)));
        }

        long totalInside = 0L;
        for (Future<Long> future : futures) {
            try {
                totalInside += future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("Задача #%d была прервана.%n", taskId);
                executor.shutdownNow();
                return;
            } catch (ExecutionException e) {
                System.out.printf("Ошибка при выполнении задачи #%d: %s%n", taskId, e.getCause().getMessage());
                executor.shutdownNow();
                return;
            }
        }

        executor.shutdown();

        double piEstimate = 4.0 * totalInside / totalPoints;

        System.out.printf("Задача #%d завершена. Приближение Pi = %.10f%n", taskId, piEstimate);
    }
}
