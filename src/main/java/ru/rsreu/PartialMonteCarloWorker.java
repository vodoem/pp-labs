package ru.rsreu;

import java.util.concurrent.Callable;

public class PartialMonteCarloWorker implements Callable<Long> {
    private final int points;
    private final int workerId;
    private final ProgressTracker progressTracker;

    public PartialMonteCarloWorker(int points, int workerId, ProgressTracker tracker) {
        this.points = points;
        this.workerId = workerId;
        this.progressTracker = tracker;
    }

    @Override
    public Long call() {
        long localInside = 0;
        int progressStep = Math.max(1, points / 100);

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

        return localInside;
    }
}
