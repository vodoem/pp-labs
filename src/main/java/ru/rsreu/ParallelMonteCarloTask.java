package ru.rsreu;

public class ParallelMonteCarloTask implements Runnable {
    private final int totalPoints;
    private final int numThreads;
    private final int taskId;
    private final LazyResultStorage storage = new LazyResultStorage();
    private volatile boolean running = true;

    public ParallelMonteCarloTask(int totalPoints, int numThreads, int taskId) {
        this.totalPoints = totalPoints;
        this.numThreads = numThreads;
        this.taskId = taskId;
    }

    public void requestStop() {
        running = false;
    }

    @Override
    public void run() {
        System.out.printf("Задача #%d начала параллельное вычисление.%n", taskId);

        int pointsPerThread = totalPoints / numThreads;
        Thread[] threads = new Thread[numThreads];
        ProgressTracker tracker = new ProgressTracker(numThreads);

        for (int i = 0; i < numThreads; i++) {
            PartialMonteCarloWorker worker = new PartialMonteCarloWorker(pointsPerThread, storage, i, tracker);
            threads[i] = new Thread(worker);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
        }

        double totalInside = storage.getStorage().stream().mapToDouble(Double::doubleValue).sum();
        double totalPointsProcessed = (double) numThreads * pointsPerThread;

        double piEstimate = 4.0 * totalInside / totalPointsProcessed;

        System.out.printf("Задача #%d завершена. Приближение Pi = %.10f%n", taskId, piEstimate);
    }
}
