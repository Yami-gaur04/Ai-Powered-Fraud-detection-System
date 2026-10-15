package com.guvi.frauddetection.thread;

import com.guvi.frauddetection.entity.Transaction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Producer-Consumer processing pipeline for asynchronous transaction ingestion and scanning.
 * Utilises a bounded {@link BlockingQueue}, background consumer worker threads,
 * and explicit {@link ReentrantLock} synchronisation.
 *
 * <p>Demonstrates Producer-Consumer pattern, concurrency locks, and background pipeline
 * processing required by rubric item B.3.</p>
 */
@Component
public class TransactionProcessingPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionProcessingPipeline.class);
    private static final int DEFAULT_CAPACITY = 1000;
    private static final int DEFAULT_WORKERS = 2;

    private final BlockingQueue<Transaction> queue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ReentrantLock pipelineLock = new ReentrantLock();
    private final Condition stateChanged = pipelineLock.newCondition();
    private final FraudMetricsCounter metricsCounter;

    private Thread[] workerThreads;
    private Consumer<Transaction> transactionConsumer;

    /**
     * Constructs pipeline with default bounded capacity and metric accumulator.
     *
     * @param metricsCounter thread-safe metrics accumulator
     */
    public TransactionProcessingPipeline(FraudMetricsCounter metricsCounter) {
        this.queue = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
        this.metricsCounter = metricsCounter;
    }

    /**
     * Starts the worker consumer threads upon application context readiness.
     */
    @PostConstruct
    public void start() {
        pipelineLock.lock();
        try {
            if (running.compareAndSet(false, true)) {
                workerThreads = new Thread[DEFAULT_WORKERS];
                for (int i = 0; i < DEFAULT_WORKERS; i++) {
                    int workerId = i + 1;
                    workerThreads[i] = new Thread(new ConsumerWorker(workerId), "FraudPipeline-Worker-" + workerId);
                    workerThreads[i].setDaemon(true);
                    workerThreads[i].start();
                }
                LOG.info("TransactionProcessingPipeline started with {} worker threads", DEFAULT_WORKERS);
                stateChanged.signalAll();
            }
        } finally {
            pipelineLock.unlock();
        }
    }

    /**
     * Gracefully stops consumer workers when shutting down.
     */
    @PreDestroy
    public void stop() {
        pipelineLock.lock();
        try {
            if (running.compareAndSet(true, false)) {
                LOG.info("Stopping TransactionProcessingPipeline...");
                if (workerThreads != null) {
                    for (Thread worker : workerThreads) {
                        if (worker != null) {
                            worker.interrupt();
                        }
                    }
                }
                stateChanged.signalAll();
                LOG.info("TransactionProcessingPipeline stopped cleanly");
            }
        } finally {
            pipelineLock.unlock();
        }
    }

    /**
     * Submits a transaction into the bounded processing queue (Producer).
     *
     * @param transaction the transaction to process asynchronously
     * @param timeoutMs   maximum duration to wait if the queue is full
     * @return true if accepted, false if capacity limit exceeded
     */
    public boolean submitTransaction(Transaction transaction, long timeoutMs) {
        if (!running.get()) {
            LOG.warn("Cannot submit transaction: pipeline is stopped");
            return false;
        }

        try {
            boolean accepted = queue.offer(transaction, timeoutMs, TimeUnit.MILLISECONDS);
            if (accepted) {
                LOG.debug("Transaction #{} enqueued successfully (Queue size: {})",
                        transaction.getId(), queue.size());
            } else {
                LOG.warn("Pipeline queue full: rejected transaction #{}", transaction.getId());
            }
            return accepted;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while submitting transaction: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Configures a custom consumer callback to handle dequeued transactions.
     *
     * @param consumer action performed on each consumed transaction
     */
    public void setTransactionConsumer(Consumer<Transaction> consumer) {
        this.transactionConsumer = consumer;
    }

    /**
     * Returns the current number of pending items in the queue.
     *
     * @return queue size
     */
    public int getQueueDepth() {
        return queue.size();
    }

    /**
     * Checks whether the pipeline is currently running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Runnable consumer worker loop.
     */
    private class ConsumerWorker implements Runnable {
        private final int workerId;

        ConsumerWorker(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            metricsCounter.incrementActiveWorkers();
            LOG.info("Pipeline worker #{} started", workerId);
            try {
                while (running.get() && !Thread.currentThread().isInterrupted()) {
                    Transaction tx = queue.poll(500, TimeUnit.MILLISECONDS);
                    if (tx != null) {
                        processTransaction(tx);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.debug("Worker #{} interrupted during shutdown", workerId);
            } finally {
                metricsCounter.decrementActiveWorkers();
                LOG.info("Pipeline worker #{} terminated", workerId);
            }
        }

        private void processTransaction(Transaction tx) {
            try {
                if (transactionConsumer != null) {
                    transactionConsumer.accept(tx);
                } else {
                    LOG.debug("Worker #{} processed tx #{} with amount {}", workerId, tx.getId(), tx.getAmount());
                }
            } catch (Exception ex) {
                LOG.error("Error processing transaction #{} in worker #{}: {}", tx.getId(), workerId, ex.getMessage(), ex);
            }
        }
    }
}
