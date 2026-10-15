package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Encapsulates contextual data required for evaluating fraud detection rules.
 * Employs diverse Java Collections including {@link Set}, {@link Queue}, {@link TreeMap}, and {@link List}.
 */
public class RuleContext {

    private final Long userId;
    private final DetectionConfig config;
    private final Double historicalAverageSpend;
    private final long totalHistoricalCount;
    private final Set<String> knownLocations;
    private final Set<String> knownDevices;
    private final Queue<LocalDateTime> recentTransactionTimestamps;
    private final TreeMap<LocalDateTime, BigDecimal> spendingTimeline;

    /**
     * Constructs a RuleContext with comprehensive user historical telemetry.
     *
     * @param userId                     the ID of the cardholder
     * @param config                     the active detection configuration thresholds
     * @param historicalAverageSpend     user's calculated average spend
     * @param totalHistoricalCount       number of prior completed transactions
     * @param knownLocations             set of verified historical location strings
     * @param knownDevices               set of verified device fingerprint strings
     * @param recentTransactionTimes     collection of timestamps within recent velocity window
     * @param historicalTransactions     list of prior user transactions
     */
    public RuleContext(Long userId,
                       DetectionConfig config,
                       Double historicalAverageSpend,
                       long totalHistoricalCount,
                       Collection<String> knownLocations,
                       Collection<String> knownDevices,
                       Collection<LocalDateTime> recentTransactionTimes,
                       List<Transaction> historicalTransactions) {
        this.userId = userId;
        this.config = config != null ? config : new DetectionConfig();
        this.historicalAverageSpend = historicalAverageSpend != null ? historicalAverageSpend : 0.0;
        this.totalHistoricalCount = Math.max(0, totalHistoricalCount);

        // HashSet for O(1) membership lookups of known locations and devices
        this.knownLocations = new HashSet<>(knownLocations != null ? knownLocations : Collections.emptyList());
        this.knownDevices = new HashSet<>(knownDevices != null ? knownDevices : Collections.emptyList());

        // ArrayDeque (Queue) for FIFO processing of sliding velocity window
        this.recentTransactionTimestamps = new ArrayDeque<>(
                recentTransactionTimes != null ? recentTransactionTimes : Collections.emptyList()
        );

        // TreeMap for chronologically sorted timeline analysis
        this.spendingTimeline = new TreeMap<>();
        if (historicalTransactions != null) {
            for (Transaction tx : historicalTransactions) {
                if (tx.getTransactionTime() != null && tx.getAmount() != null) {
                    this.spendingTimeline.put(tx.getTransactionTime(), tx.getAmount());
                }
            }
        }
    }

    public Long getUserId() {
        return userId;
    }

    public DetectionConfig getConfig() {
        return config;
    }

    public Double getHistoricalAverageSpend() {
        return historicalAverageSpend;
    }

    public long getTotalHistoricalCount() {
        return totalHistoricalCount;
    }

    public Set<String> getKnownLocations() {
        return Collections.unmodifiableSet(knownLocations);
    }

    public Set<String> getKnownDevices() {
        return Collections.unmodifiableSet(knownDevices);
    }

    public Queue<LocalDateTime> getRecentTransactionTimestamps() {
        return recentTransactionTimestamps;
    }

    public TreeMap<LocalDateTime, BigDecimal> getSpendingTimeline() {
        return spendingTimeline;
    }

    public boolean hasHistory() {
        return totalHistoricalCount > 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private DetectionConfig config;
        private Double historicalAverageSpend = 0.0;
        private long totalHistoricalCount = 0;
        private Collection<String> knownLocations = new ArrayList<>();
        private Collection<String> knownDevices = new ArrayList<>();
        private Collection<LocalDateTime> recentTransactionTimes = new ArrayList<>();
        private List<Transaction> historicalTransactions = new ArrayList<>();

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder config(DetectionConfig config) {
            this.config = config;
            return this;
        }

        public Builder historicalAverageSpend(Double historicalAverageSpend) {
            this.historicalAverageSpend = historicalAverageSpend;
            return this;
        }

        public Builder totalHistoricalCount(long totalHistoricalCount) {
            this.totalHistoricalCount = totalHistoricalCount;
            return this;
        }

        public Builder knownLocations(Collection<String> knownLocations) {
            this.knownLocations = knownLocations;
            return this;
        }

        public Builder knownDevices(Collection<String> knownDevices) {
            this.knownDevices = knownDevices;
            return this;
        }

        public Builder recentTransactionTimes(Collection<LocalDateTime> recentTransactionTimes) {
            this.recentTransactionTimes = recentTransactionTimes;
            return this;
        }

        public Builder historicalTransactions(List<Transaction> historicalTransactions) {
            this.historicalTransactions = historicalTransactions;
            return this;
        }

        public RuleContext build() {
            return new RuleContext(userId, config, historicalAverageSpend, totalHistoricalCount,
                    knownLocations, knownDevices, recentTransactionTimes, historicalTransactions);
        }
    }
}
