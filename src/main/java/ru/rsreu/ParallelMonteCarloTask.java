package ru.rsreu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelMonteCarloTask implements Runnable {
    private final int totalPoints;
    private final int numThreads;
    private final int taskId;
    private final Semaphore semaphore;

    public ParallelMonteCarloTask(int totalPoints, int numThreads, int taskId, Semaphore semaphore) {
        this.totalPoints = totalPoints;
        this.numThreads = numThreads;
        this.taskId = taskId;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            if (totalPoints <= 0) {
                System.out.printf("Задача #%d завершена без вычислений: количество точек должно быть положительным.%n", taskId);
                return;
            }

            System.out.printf("Задача #%d начала параллельное вычисление.%n", taskId);

            int actualWorkers = Math.min(numThreads, totalPoints);
            ExecutorService executor = Executors.newFixedThreadPool(actualWorkers);
            ReentrantLock progressLock = new ReentrantLock();
            ProgressTracker tracker = new ProgressTracker(actualWorkers, progressLock);
            AtomicInteger remainingWorkers = new AtomicInteger(actualWorkers);
            AtomicLong finalCompletionTime = new AtomicLong();
            CountDownLatch completionLatch = new CountDownLatch(1);

            int basePointsPerWorker = totalPoints / actualWorkers;
            int remainder = totalPoints % actualWorkers;

            List<Future<Long>> futures = new ArrayList<>();
            for (int i = 0; i < actualWorkers; i++) {
                int workerPoints = basePointsPerWorker + (i < remainder ? 1 : 0);
                futures.add(executor.submit(new PartialMonteCarloWorker(
                        workerPoints,
                        i,
                        taskId,
                        tracker,
                        remainingWorkers,
                        finalCompletionTime,
                        completionLatch)));
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
        } finally {
            semaphore.release();
        }
    }
}
