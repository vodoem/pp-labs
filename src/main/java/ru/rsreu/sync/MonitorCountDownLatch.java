package ru.rsreu.sync;

import java.util.concurrent.TimeUnit;

/**
 * Monitor-based CountDownLatch implementation.
 */
public class MonitorCountDownLatch {
    private int count;

    public MonitorCountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Значение счётчика не может быть отрицательным");
        }
        this.count = count;
    }

    public void await() throws InterruptedException {
        synchronized (this) {
            while (count > 0) {
                wait();
            }
        }
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        long nanosTimeout = unit.toNanos(timeout);
        synchronized (this) {
            if (count == 0) {
                return true;
            }
            if (nanosTimeout <= 0) {
                return false;
            }
            long deadline = System.nanoTime() + nanosTimeout;
            while (count > 0) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    return false;
                }
                long millis = remaining / 1_000_000L;
                int nanos = (int) (remaining % 1_000_000L);
                wait(millis, nanos);
            }
            return true;
        }
    }

    public void countDown() {
        synchronized (this) {
            if (count == 0) {
                return;
            }
            count--;
            if (count == 0) {
                notifyAll();
            }
        }
    }

    public boolean isLocked() {
        synchronized (this) {
            return count > 0;
        }
    }

    public int getCount() {
        synchronized (this) {
            return count;
        }
    }
}
