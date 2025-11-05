package ru.rsreu.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class MonitorSemaphoreTest {

    @Test
    void testAcquireReleaseSingleThread() throws InterruptedException {
        MonitorSemaphore semaphore = new MonitorSemaphore(1);
        semaphore.acquire();
        assertEquals(0, semaphore.availablePermits());
        semaphore.release();
        assertEquals(1, semaphore.availablePermits());
    }

    @Test
    void testAcquireBlocksUntilRelease() throws InterruptedException {
        MonitorSemaphore semaphore = new MonitorSemaphore(1);
        semaphore.acquire();

        AtomicBoolean completed = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            try {
                semaphore.acquire();
                completed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.start();

        Thread.sleep(100);
        assertFalse(completed.get());

        semaphore.release();
        worker.join(Duration.ofSeconds(1).toMillis());
        assertTrue(completed.get());
    }

    @Test
    void testAcquireWithTimeout() throws InterruptedException {
        MonitorSemaphore semaphore = new MonitorSemaphore(0);
        boolean acquired = semaphore.acquire(100, TimeUnit.MILLISECONDS);
        assertFalse(acquired);
        assertEquals(0, semaphore.availablePermits());
    }

    @Test
    void testTryAcquire() {
        MonitorSemaphore semaphore = new MonitorSemaphore(1);
        assertTrue(semaphore.tryAcquire());
        assertFalse(semaphore.tryAcquire());
        semaphore.release();
        assertTrue(semaphore.tryAcquire());
    }

    @Test
    void testAcquireInterrupted() throws InterruptedException {
        MonitorSemaphore semaphore = new MonitorSemaphore(0);
        AtomicBoolean interrupted = new AtomicBoolean(false);

        Thread worker = new Thread(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                interrupted.set(true);
                Thread.currentThread().interrupt();
            }
        });
        worker.start();

        Thread.sleep(100);
        worker.interrupt();

        worker.join(Duration.ofSeconds(1).toMillis());
        assertTrue(interrupted.get());
        assertTrue(worker.isInterrupted());
    }
}
