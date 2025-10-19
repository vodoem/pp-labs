package ru.rsreu;

public class PartialMonteCarloWorker implements Runnable {
    private final int points;
    private final LazyResultStorage storage;
    private final int workerId;
    private volatile boolean running = true;
    private long insideCircle = 0;
    private final ProgressTracker progressTracker;

    public PartialMonteCarloWorker(int points, LazyResultStorage storage, int workerId, ProgressTracker tracker) {
        this.points = points;
        this.storage = storage;
        this.workerId = workerId;
        this.progressTracker = tracker;
    }

    public void requestStop() {
        running = false;
    }

    @Override
    public void run() {
        long localInside = 0;
        int progressStep = points / 100;

        for (int i = 0; i < points && running; i++) {
            double x = Math.random();
            double y = Math.random();
            if (x * x + y * y <= 1.0) localInside++;

            if (i % progressStep == 0) {
                progressTracker.update(workerId, (double) i / points);
            }
        }

        storage.addResult(localInside);
        progressTracker.update(workerId, 1.0);
    }
}
