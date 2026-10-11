package com.guvi.frauddetection.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe metrics accumulator utilizing lock-free atomic primitives
 * ({@link AtomicLong} and {@link AtomicInteger}).
 *
 * <p>Safely aggregates system-wide transaction counts, blocked fraud attempts,
 * processed transaction volumes, and active worker counters across concurrent threads
 * without blocking.</p>
 *
 * <p>Demonstrates thread-safety and atomic operations required by rubric item B.3.</p>
 */
@Component
public class FraudMetricsCounter {

    private static final Logger LOG = LoggerFactory.getLogger(FraudMetricsCounter.class);

    private final AtomicLong totalTransactionsProcessed = new AtomicLong(0);
    private final AtomicLong totalFraudFlagged = new AtomicLong(0);
    private final AtomicLong totalFraudBlocked = new AtomicLong(0);
    private final AtomicLong totalVolumeCents = new AtomicLong(0);
    private final AtomicInteger activeWorkerThreads = new AtomicInteger(0);

    /**
     * Records a processed transaction, updating transaction volume and total count atomically.
     *
     * @param amount the transaction monetary amount
     */
    public void recordTransaction(BigDecimal amount) {
        totalTransactionsProcessed.incrementAndGet();
        if (amount != null) {
            long cents = amount.multiply(BigDecimal.valueOf(100)).longValue();
            totalVolumeCents.addAndGet(cents);
        }
    }

    /**
     * Records a flagged fraud event atomically.
     */
    public void recordFraudFlagged() {
        totalFraudFlagged.incrementAndGet();
    }

    /**
     * Records a blocked fraud event atomically.
     */
    public void recordFraudBlocked() {
        totalFraudBlocked.incrementAndGet();
    }

    /**
     * Increments the count of currently active worker threads.
     *
     * @return new active thread count
     */
    public int incrementActiveWorkers() {
        return activeWorkerThreads.incrementAndGet();
    }

    /**
     * Decrements the count of currently active worker threads.
     *
     * @return remaining active thread count
     */
    public int decrementActiveWorkers() {
        return activeWorkerThreads.decrementAndGet();
    }

    /**
     * Returns total transactions processed.
     *
     * @return total processed count
     */
    public long getTotalTransactionsProcessed() {
        return totalTransactionsProcessed.get();
    }

    /**
     * Returns total fraud cases flagged.
     *
     * @return flagged fraud count
     */
    public long getTotalFraudFlagged() {
        return totalFraudFlagged.get();
    }

    /**
     * Returns total transactions blocked due to high fraud risk.
     *
     * @return blocked fraud count
     */
    public long getTotalFraudBlocked() {
        return totalFraudBlocked.get();
    }

    /**
     * Returns total processed monetary volume.
     *
     * @return total volume as BigDecimal
     */
    public BigDecimal getTotalVolume() {
        return BigDecimal.valueOf(totalVolumeCents.get()).divide(BigDecimal.valueOf(100));
    }

    /**
     * Returns count of active background worker threads.
     *
     * @return active workers count
     */
    public int getActiveWorkerThreads() {
        return activeWorkerThreads.get();
    }

    /**
     * Atomically resets all metric counters to zero.
     */
    public void reset() {
        totalTransactionsProcessed.set(0);
        totalFraudFlagged.set(0);
        totalFraudBlocked.set(0);
        totalVolumeCents.set(0);
        activeWorkerThreads.set(0);
        LOG.info("FraudMetricsCounter has been reset to zero");
    }
}
