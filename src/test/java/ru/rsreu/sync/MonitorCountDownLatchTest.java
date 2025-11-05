package ru.rsreu.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class MonitorCountDownLatchTest {

    @Test
    void testAwaitCompletesAfterCountDown() throws InterruptedException {
        MonitorCountDownLatch latch = new MonitorCountDownLatch(2);
        AtomicBoolean completed = new AtomicBoolean(false);

        Thread waiting = new Thread(() -> {
            try {
                latch.await();
                completed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiting.start();

        Thread.sleep(50);
        assertFalse(completed.get());

        latch.countDown();
        assertFalse(completed.get());
        latch.countDown();

        waiting.join(Duration.ofSeconds(1).toMillis());
        assertTrue(completed.get());
        assertFalse(latch.isLocked());
        assertEquals(0, latch.getCount());
    }

    @Test
    void testAwaitTimeout() throws InterruptedException {
        MonitorCountDownLatch latch = new MonitorCountDownLatch(1);
        boolean result = latch.await(100, TimeUnit.MILLISECONDS);
        assertFalse(result);
        assertTrue(latch.isLocked());
    }

    @Test
    void testAwaitInterrupted() throws InterruptedException {
        MonitorCountDownLatch latch = new MonitorCountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);

        Thread waiting = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                interrupted.set(true);
                Thread.currentThread().interrupt();
            }
        });
        waiting.start();

        Thread.sleep(100);
        waiting.interrupt();
        waiting.join(Duration.ofSeconds(1).toMillis());
        assertTrue(interrupted.get());
        assertTrue(waiting.isInterrupted());
    }
}
