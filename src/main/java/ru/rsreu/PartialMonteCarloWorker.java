package ru.rsreu;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PartialMonteCarloWorker implements Callable<Long> {
    private final int points;
    private final int workerId;
    private final int taskId;
    private final ProgressTracker progressTracker;
    private final AtomicInteger remainingWorkers;
    private final AtomicLong finalCompletionTime;
    private final CountDownLatch completionLatch;

    public PartialMonteCarloWorker(int points,
                                  int workerId,
                                  int taskId,
                                  ProgressTracker tracker,
                                  AtomicInteger remainingWorkers,
                                  AtomicLong finalCompletionTime,
                                  CountDownLatch completionLatch) {
        this.points = points;
        this.workerId = workerId;
        this.taskId = taskId;
        this.progressTracker = tracker;
        this.remainingWorkers = remainingWorkers;
        this.finalCompletionTime = finalCompletionTime;
        this.completionLatch = completionLatch;
    }

    @Override
    public Long call() {
        long localInside = 0;
        int progressStep = Math.max(1, points / 100);

        try {
            for (int i = 0; i < points; i++) {
                double x = Math.random();
                double y = Math.random();
                if (x * x + y * y <= 1.0) {
                    localInside++;
                }

                if ((i + 1) % progressStep == 0 || i == points - 1) {
                    progressTracker.update(workerId, (double) (i + 1) / points);
                }
            }
        } finally {
            long finishTime = System.nanoTime();
            int left = remainingWorkers.decrementAndGet();
            if (left == 0) {
                finalCompletionTime.set(finishTime);
                completionLatch.countDown();
            } else {
                try {
                    completionLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.printf("Задача #%d, поток #%d был прерван при ожидании завершения.%n", taskId, workerId);
                }
            }

            long delayNanos = Math.max(0L, finalCompletionTime.get() - finishTime);
            double delayMillis = delayNanos / 1_000_000.0;
            System.out.printf("Задача #%d, поток #%d: задержка завершения %.3f мс.%n", taskId, workerId, delayMillis);
        }

        return localInside;
    }
}
