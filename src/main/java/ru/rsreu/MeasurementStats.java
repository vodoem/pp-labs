package ru.rsreu;

import java.util.List;

public class MeasurementStats {
    private final List<Long> executionTimes;
    private final List<Double> errors;
    private final double epsilon;

    public MeasurementStats(List<Long> executionTimes,
                            List<Double> errors, double epsilon) {
        this.executionTimes = executionTimes;
        this.errors = errors;
        this.epsilon = epsilon;
    }

    public double getAverageTimeMs() {
        return executionTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
    }


    public double getAverageError() {
        return errors.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public double getTimeStandardDeviationMs() {
        double mean = getAverageTimeMs();
        double sum = executionTimes.stream()
                .mapToDouble(time -> Math.pow(time / 1_000_000.0 - mean, 2))
                .sum();
        return Math.sqrt(sum / executionTimes.size());
    }

    public void printStats() {
        System.out.printf("=== Статистика для epsilon = %.10f ===%n", epsilon);
        System.out.printf("Количество запусков: %d%n", executionTimes.size());
        System.out.printf("Среднее время: %.3f ms ± %.3f ms%n",
                getAverageTimeMs(), getTimeStandardDeviationMs());
        System.out.printf("Средняя погрешность: %.10f%n", getAverageError());
        System.out.println("-----------------------------------");

        // Детальная информация по каждому запуску
        for (int i = 0; i < executionTimes.size(); i++) {
            System.out.printf("Запуск %d: %.3f ms, погрешность: %.10f%n",
                    i + 1,
                    executionTimes.get(i) / 1_000_000.0,
                    errors.get(i));
        }
    }
}