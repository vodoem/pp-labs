package ru.rsreu.sync;

import java.util.concurrent.TimeUnit;

/**
 * Monitor-based semaphore implementation supporting blocking, timed, and non-blocking acquisition.
 */
public class MonitorSemaphore {
    private int permits;

    public MonitorSemaphore(int permits) {
        if (permits < 0) {
            throw new IllegalArgumentException("Количество разрешений не может быть отрицательным");
        }
        this.permits = permits;
    }

    public void acquire() throws InterruptedException {
        synchronized (this) {
            while (permits == 0) {
                wait();
            }
            permits--;
        }
    }

    public boolean acquire(long timeout, TimeUnit unit) throws InterruptedException {
        long nanosTimeout = unit.toNanos(timeout);
        synchronized (this) {
            if (permits > 0) {
                permits--;
                return true;
            }
            if (nanosTimeout <= 0) {
                return false;
            }

            long deadline = System.nanoTime() + nanosTimeout;
            long remaining = nanosTimeout;
            while (true) {
                long millis = TimeUnit.NANOSECONDS.toMillis(remaining);
                int nanos = (int) (remaining - TimeUnit.MILLISECONDS.toNanos(millis));
                wait(millis, nanos);

                if (permits > 0) {
                    permits--;
                    return true;
                }

                remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    return false;
                }
            }
        }
    }

    public boolean tryAcquire() {
        synchronized (this) {
            if (permits > 0) {
                permits--;
                return true;
            }
            return false;
        }
    }

    public void release() {
        synchronized (this) {
            permits++;
            notifyAll();
        }
    }

    public int availablePermits() {
        synchronized (this) {
            return permits;
        }
    }
}
