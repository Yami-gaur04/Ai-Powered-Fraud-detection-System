package com.guvi.frauddetection.thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multithreading - FraudMetricsCounter Concurrency Test")
class FraudMetricsCounterTest {

    @Test
    @DisplayName("Concurrent increments across 10 threads produce exact atomic counts")
    void testConcurrentUpdates() throws InterruptedException {
        FraudMetricsCounter counter = new FraudMetricsCounter();
        int threadCount = 10;
        int operationsPerThread = 500;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.recordTransaction(BigDecimal.valueOf(10.00));
                        if (j % 5 == 0) {
                            counter.recordFraudFlagged();
                        }
                        if (j % 10 == 0) {
                            counter.recordFraudBlocked();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // unblock all threads simultaneously
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All threads should complete within timeout");
        long expectedTxCount = (long) threadCount * operationsPerThread;
        assertEquals(expectedTxCount, counter.getTotalTransactionsProcessed());
        assertEquals(expectedTxCount * 10.0, counter.getTotalVolume().doubleValue(), 0.01);
        assertEquals(threadCount * (operationsPerThread / 5), counter.getTotalFraudFlagged());
        assertEquals(threadCount * (operationsPerThread / 10), counter.getTotalFraudBlocked());
    }

    @Test
    @DisplayName("Reset resets all atomic counters to zero")
    void testReset() {
        FraudMetricsCounter counter = new FraudMetricsCounter();
        counter.recordTransaction(BigDecimal.valueOf(100.00));
        counter.recordFraudFlagged();
        counter.recordFraudBlocked();

        assertTrue(counter.getTotalTransactionsProcessed() > 0);
        counter.reset();

        assertEquals(0, counter.getTotalTransactionsProcessed());
        assertEquals(0, counter.getTotalFraudFlagged());
        assertEquals(0, counter.getTotalFraudBlocked());
        assertEquals(0.0, counter.getTotalVolume().doubleValue());
    }
}
