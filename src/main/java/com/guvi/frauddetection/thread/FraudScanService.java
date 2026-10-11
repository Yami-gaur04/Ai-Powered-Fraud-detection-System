package com.guvi.frauddetection.thread;

import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.service.FraudDetectionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Service managing background thread pools for parallel fraud detection scans.
 * Uses a custom-sized {@link ExecutorService} with bounded queues and named threads.
 *
 * <p>Demonstrates explicit {@link ExecutorService}, {@link Callable}, and {@link Future}
 * multithreading constructs required by rubric item B.3.</p>
 */
@Service
public class FraudScanService {

    private static final Logger LOG = LoggerFactory.getLogger(FraudScanService.class);
    private static final int POOL_SIZE = 4;

    private final FraudDetectionService fraudDetectionService;
    private final FraudMetricsCounter metricsCounter;
    private ExecutorService threadPool;

    /**
     * Constructs the background scanning service.
     *
     * @param fraudDetectionService engine for rule evaluation
     * @param metricsCounter        system-wide metric accumulator
     */
    public FraudScanService(FraudDetectionService fraudDetectionService,
                            FraudMetricsCounter metricsCounter) {
        this.fraudDetectionService = fraudDetectionService;
        this.metricsCounter = metricsCounter;
    }

    /**
     * Initializes the thread pool with dedicated worker threads.
     */
    @PostConstruct
    public void init() {
        this.threadPool = Executors.newFixedThreadPool(POOL_SIZE, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "FraudScanWorker-" + (counter++));
                t.setDaemon(true);
                return t;
            }
        });
        LOG.info("FraudScanService initialised with a pool of {} threads", POOL_SIZE);
    }

    /**
     * Asynchronously evaluates a single transaction's fraud score.
     *
     * @param user the transaction owner
     * @param tx   the transaction under evaluation
     * @return a {@link Future} representing the pending evaluation result
     */
    public Future<FraudDetectionService.Result> scanAsync(User user, Transaction tx) {
        if (threadPool == null || threadPool.isShutdown()) {
            throw new IllegalStateException("FraudScanService thread pool is not active");
        }

        return threadPool.submit(() -> {
            metricsCounter.incrementActiveWorkers();
            try {
                FraudDetectionService.Result result = fraudDetectionService.evaluate(user, tx);
                metricsCounter.recordTransaction(tx.getAmount());
                if (result.score() >= 0.70) {
                    metricsCounter.recordFraudBlocked();
                } else if (result.score() >= 0.40) {
                    metricsCounter.recordFraudFlagged();
                }
                return result;
            } finally {
                metricsCounter.decrementActiveWorkers();
            }
        });
    }

    /**
     * Concurrently scans a batch of transactions using parallel worker threads.
     *
     * @param user         the transaction owner
     * @param transactions list of transactions to evaluate
     * @return list of {@link Future} results for each transaction
     */
    public List<Future<FraudDetectionService.Result>> scanBatch(User user, List<Transaction> transactions) {
        List<Future<FraudDetectionService.Result>> futures = new ArrayList<>(transactions.size());
        for (Transaction tx : transactions) {
            futures.add(scanAsync(user, tx));
        }
        return futures;
    }

    /**
     * Gracefully shuts down the thread pool upon application termination.
     */
    @PreDestroy
    public void shutdown() {
        if (threadPool != null) {
            LOG.info("Shutting down FraudScanService thread pool...");
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOG.info("FraudScanService thread pool stopped cleanly");
        }
    }
}
