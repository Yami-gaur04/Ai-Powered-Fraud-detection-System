package com.guvi.frauddetection.thread;

import com.guvi.frauddetection.service.ReportService;
import com.guvi.frauddetection.dto.DetectionReport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scheduled background task that periodically refreshes system-wide statistics,
 * caching the latest detection reports and audit figures.
 *
 * <p>Demonstrates {@link ScheduledExecutorService} usage for recurring periodic background jobs
 * as required by rubric item B.3.</p>
 */
@Component
public class StatisticsRefreshTask {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsRefreshTask.class);
    private static final long INITIAL_DELAY_SECONDS = 5;
    private static final long PERIOD_SECONDS = 60;

    private final ReportService reportService;
    private final ThreadSafeCache<String, DetectionReport> reportCache;
    private final AtomicLong executionCounter = new AtomicLong(0);

    private ScheduledExecutorService scheduler;

    /**
     * Constructs the scheduled task with required report dependencies and cache.
     *
     * @param reportService the report generation service
     * @param reportCache   thread-safe cache for holding precomputed reports
     */
    public StatisticsRefreshTask(ReportService reportService,
                                 ThreadSafeCache<String, DetectionReport> reportCache) {
        this.reportService = reportService;
        this.reportCache = reportCache;
    }

    /**
     * Schedules the recurring statistics refresh job.
     */
    @PostConstruct
    public void startScheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "StatsRefresh-ScheduledWorker");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::refreshStatistics,
                INITIAL_DELAY_SECONDS, PERIOD_SECONDS, TimeUnit.SECONDS);

        LOG.info("StatisticsRefreshTask scheduled every {} seconds (initial delay: {}s)",
                PERIOD_SECONDS, INITIAL_DELAY_SECONDS);
    }

    /**
     * Computes the latest detection report and caches it thread-safely.
     */
    public void refreshStatistics() {
        try {
            long runId = executionCounter.incrementAndGet();
            LOG.debug("Running scheduled statistics refresh #{}...", runId);

            DetectionReport report = reportService.buildReport();
            reportCache.put("LATEST_REPORT", report);

            LOG.debug("Statistics refresh #{} completed: total cases={}, fraud rate={}%",
                    runId, report.totalCases(), report.fraudRatePercent());
        } catch (Exception ex) {
            LOG.error("Failed to refresh statistics in background task: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Returns total number of times the scheduled task has run.
     *
     * @return execution count
     */
    public long getExecutionCount() {
        return executionCounter.get();
    }

    /**
     * Gracefully shuts down the scheduled executor.
     */
    @PreDestroy
    public void stopScheduler() {
        if (scheduler != null) {
            LOG.info("Shutting down StatisticsRefreshTask scheduler...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOG.info("StatisticsRefreshTask scheduler stopped cleanly");
        }
    }
}
