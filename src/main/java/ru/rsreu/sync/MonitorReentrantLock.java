package ru.rsreu.sync;

import java.util.concurrent.TimeUnit;

/**
 * Monitor-based reentrant lock implementation.
 */
public class MonitorReentrantLock {
    private Thread owner;
    private int holdCount;

    public void lock() throws InterruptedException {
        synchronized (this) {
            Thread current = Thread.currentThread();
            if (current == owner) {
                holdCount++;
                return;
            }
            while (owner != null) {
                wait();
            }
            owner = current;
            holdCount = 1;
        }
    }

    public boolean lock(long timeout, TimeUnit unit) throws InterruptedException {
        long nanosTimeout = unit.toNanos(timeout);
        synchronized (this) {
            Thread current = Thread.currentThread();
            if (current == owner) {
                holdCount++;
                return true;
            }
            if (owner == null) {
                owner = current;
                holdCount = 1;
                return true;
            }
            if (nanosTimeout <= 0) {
                return false;
            }
            long deadline = System.nanoTime() + nanosTimeout;
            while (owner != null) {
                long remaining = deadline - System.nanoTime();
                if (owner == null) {
                    break;
                }
                if (remaining <= 0) {
                    return false;
                }
                long millis = remaining / 1_000_000L;
                int nanos = (int) (remaining % 1_000_000L);
                wait(millis, nanos);
            }
            if (owner == null) {
                owner = current;
                holdCount = 1;
                return true;
            }
            return false;
        }
    }

    public boolean tryLock() {
        synchronized (this) {
            Thread current = Thread.currentThread();
            if (owner == null) {
                owner = current;
                holdCount = 1;
                return true;
            }
            if (owner == current) {
                holdCount++;
                return true;
            }
            return false;
        }
    }

    public void unlock() {
        synchronized (this) {
            if (Thread.currentThread() != owner) {
                throw new IllegalMonitorStateException("Текущий поток не владеет блокировкой");
            }
            holdCount--;
            if (holdCount == 0) {
                owner = null;
                notifyAll();
            }
        }
    }

    public boolean isLocked() {
        synchronized (this) {
            return owner != null;
        }
    }
}
