package ru.rsreu;

public class Main {
    public static void main(String[] args) {
        PerformanceMeasurer measurer = new PerformanceMeasurer(3, 5);

        try {
            // Подбираем epsilon для времени 1-10 секунд (1000-10000 ms)
            double optimalEpsilon = measurer.findEpsilonForTargetTime(0.000001, 1000, 10000);

            System.out.printf("\nНайденный оптимальный epsilon: %.10f%n", optimalEpsilon);

            // Финальные измерения с полной статистикой
            MeasurementStats finalStats = measurer.measurePerformance(optimalEpsilon);
            finalStats.printStats();

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}