package ru.rsreu;

import java.util.concurrent.ThreadLocalRandom;

public class MonteCarloPiCalculator {
    private final double epsilon;

    public MonteCarloPiCalculator(double epsilon) {
        this.epsilon = epsilon;
    }

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
}