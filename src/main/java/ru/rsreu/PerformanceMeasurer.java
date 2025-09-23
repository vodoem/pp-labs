package ru.rsreu;

import java.util.ArrayList;
import java.util.List;

public class PerformanceMeasurer {
    private final int measurementRuns;

    public PerformanceMeasurer(int measurementRuns) {
        this.measurementRuns = measurementRuns;
    }

    public MeasurementStats measurePerformance(double epsilon) {
        List<Long> executionTimes = new ArrayList<>();
        List<Double> errors = new ArrayList<>();

        // Основные измерения
        for (int i = 0; i < measurementRuns; i++) {
            MonteCarloPiCalculator calc = new MonteCarloPiCalculator(epsilon);

            long startTime = System.nanoTime();
            double piValue = calc.calculatePiWithProgress(i);
            long endTime = System.nanoTime();

            long executionTime = endTime - startTime;
            executionTimes.add(executionTime);

            errors.add(Math.abs(piValue - Math.PI));
        }

        return new MeasurementStats(executionTimes, errors, epsilon);
    }


    public double findEpsilonForTargetTime(double initialEpsilon, long targetMinMillis, long targetMaxMillis) {
        double epsilon = initialEpsilon;
        int attempts = 0;
        final int maxAttempts = 20;

        while (attempts++ < maxAttempts) {
            // Быстрая проверка с меньшим количеством запусков для подбора
            MeasurementStats stats = quickMeasure(epsilon, 2);
            double avgTimeMs = stats.getAverageTimeMs();

            System.out.printf("Epsilon: %.10f, Avg time: %.3f ms%n", epsilon, avgTimeMs);

            if (avgTimeMs >= targetMinMillis && avgTimeMs <= targetMaxMillis) {
                return epsilon;
            } else if (avgTimeMs < targetMinMillis) {
                epsilon /= 2.0;
            } else {
                epsilon *= 2.0;
            }
        }

        throw new RuntimeException("Не удалось подобрать epsilon за " + maxAttempts + " попыток");
    }

    private MeasurementStats quickMeasure(double epsilon, int runs) {
        List<Long> executionTimes = new ArrayList<>();
        List<Double> errors = new ArrayList<>();

        for (int i = 0; i < runs; i++) {
            MonteCarloPiCalculator calc = new MonteCarloPiCalculator(epsilon);

            long startTime = System.nanoTime();
            double piValue = calc.calculatePi();
            long endTime = System.nanoTime();

            executionTimes.add(endTime - startTime);
            errors.add(Math.abs(piValue - Math.PI));
        }

        return new MeasurementStats(executionTimes, errors, epsilon);
    }
}
