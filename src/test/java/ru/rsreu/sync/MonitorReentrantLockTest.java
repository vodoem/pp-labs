package ru.rsreu.sync;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class MonitorReentrantLockTest {

    @Test
    void testReentrancy() throws InterruptedException {
        MonitorReentrantLock lock = new MonitorReentrantLock();
        lock.lock();
        try {
            assertTrue(lock.tryLock());
        } finally {
            lock.unlock();
            lock.unlock();
        }
    }

    @Test
    void testLockBlocksUntilUnlock() throws InterruptedException {
        MonitorReentrantLock lock = new MonitorReentrantLock();
        AtomicBoolean acquired = new AtomicBoolean(false);

        lock.lock();
        Thread worker = new Thread(() -> {
            try {
                lock.lock();
                acquired.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (acquired.get()) {
                    lock.unlock();
                }
            }
        });
        worker.start();

        Thread.sleep(100);
        assertFalse(acquired.get());

        lock.unlock();
        worker.join(Duration.ofSeconds(1).toMillis());
        assertTrue(acquired.get());
    }

    @Test
    void testTryLock() throws InterruptedException {
        MonitorReentrantLock lock = new MonitorReentrantLock();
        assertTrue(lock.tryLock());
        try {
            assertFalse(lock.tryLock());
        } finally {
            lock.unlock();
        }
    }

    @Test
    void testTimedLockTimeout() throws InterruptedException {
        MonitorReentrantLock lock = new MonitorReentrantLock();
        lock.lock();
        AtomicBoolean acquired = new AtomicBoolean(false);

        Thread worker = new Thread(() -> {
            try {
                acquired.set(lock.lock(100, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.start();
        worker.join(Duration.ofSeconds(1).toMillis());
        assertFalse(acquired.get());
        lock.unlock();
    }

    @Test
    void testLockInterrupted() throws InterruptedException {
        MonitorReentrantLock lock = new MonitorReentrantLock();
        lock.lock();

        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            try {
                lock.lock();
                fail("Lock acquisition should not succeed");
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
        lock.unlock();
    }
}
