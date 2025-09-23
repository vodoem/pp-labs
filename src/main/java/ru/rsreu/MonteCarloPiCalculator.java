package ru.rsreu;

import java.util.concurrent.ThreadLocalRandom;

public class MonteCarloPiCalculator {
    private final double epsilon;

    public MonteCarloPiCalculator(double epsilon) {
        this.epsilon = epsilon;
    }

    // Обычный метод без прогресса (для PerformanceMeasurer)
    public double calculatePi() {
        long insideCircle = 0;
        long totalPoints = 0;
        double piEstimate = 0.0;

        while (true) {
            double x = ThreadLocalRandom.current().nextDouble();
            double y = ThreadLocalRandom.current().nextDouble();
            totalPoints++;

            if (x * x + y * y <= 1.0) {
                insideCircle++;
            }

            if (totalPoints % 100_000 == 0) {
                piEstimate = 4.0 * insideCircle / totalPoints;
                if (Math.abs(piEstimate - Math.PI) < epsilon) {
                    break;
                }
            }
        }

        return 4.0 * insideCircle / totalPoints;
    }

    // Новый метод с выводом прогресса
    public double calculatePiWithProgress(int runIndex) {
        System.out.printf("%nЗапуск вычисления #%d...%n", runIndex);

        long insideCircle = 0;
        long totalPoints = 0;
        double piEstimate = 0.0;

        int progressSteps = 20;   // хотим 20 сообщений
        int nextProgress = 1;     // следующий процент к печати
        int lastPercent = 0;

        while (true) {
            double x = ThreadLocalRandom.current().nextDouble();
            double y = ThreadLocalRandom.current().nextDouble();
            totalPoints++;

            if (x * x + y * y <= 1.0) {
                insideCircle++;
            }

            if (totalPoints % 100_000 == 0) {
                piEstimate = 4.0 * insideCircle / totalPoints;
                double error = Math.abs(piEstimate - Math.PI);

                // считаем прогресс как отношение epsilon / error
                double relative = Math.min(1.0,
                        Math.log(1 / error) / Math.log(1 / epsilon));
                int percent = (int) (relative * 100);

                if (percent >= nextProgress * (100 / progressSteps) && nextProgress <= progressSteps && percent>lastPercent) {
                    System.out.printf("Вычисление #%d: прогресс %d%%%n", runIndex, percent);
                    nextProgress++;
                    lastPercent = percent;
                }

                if (error < epsilon) {
                    break;
                }
            }
        }

        return 4.0 * insideCircle / totalPoints;
    }
}
