package ru.rsreu;

public class MonteCarloTask implements Runnable {
    private final double epsilon;
    private final int taskId;
    private volatile boolean running = true;

    public MonteCarloTask(double epsilon, int taskId) {
        this.epsilon = epsilon;
        this.taskId = taskId;
    }

    public void requestStop() {
        running = false;
    }

    @Override
    public void run() {
        MonteCarloPiCalculator calculator = new MonteCarloPiCalculator(epsilon);

        long insideCircle = 0;
        long totalPoints = 0;
        double piEstimate = 0.0;

        int progressSteps = 20;
        int nextProgress = 1;
        int lastPercent = 0;

        System.out.printf("Задача #%d начала вычисления с epsilon = %.10f%n", taskId, epsilon);

        while (running) {
            double x = Math.random();
            double y = Math.random();
            totalPoints++;

            if (x * x + y * y <= 1.0) {
                insideCircle++;
            }

            if (totalPoints % 100_000 == 0) {
                piEstimate = 4.0 * insideCircle / totalPoints;
                double error = Math.abs(piEstimate - Math.PI);

                double relative = Math.min(1.0, Math.log(1 / error) / Math.log(1 / epsilon));
                int percent = (int) (relative * 100);

                if (percent >= nextProgress * (100 / progressSteps) && nextProgress <= progressSteps && percent > lastPercent) {
                    System.out.printf("Задача #%d: прогресс %d%%%n", taskId, percent);
                    nextProgress++;
                    lastPercent = percent;
                }

                if (error < epsilon) {
                    break;
                }
            }
        }

        if (!running) {
            System.out.printf("Задача #%d была остановлена пользователем.%n", taskId);
        } else {
            System.out.printf("Задача #%d завершена. Приближение Pi = %.10f%n", taskId, piEstimate);
        }
    }
}
