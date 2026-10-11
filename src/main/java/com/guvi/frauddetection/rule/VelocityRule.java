package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.time.LocalDateTime;
import java.util.Queue;

/**
 * Evaluates high transaction frequency within a sliding 1-hour time window.
 * Utilizes {@link Queue} (ArrayDeque) and sliding-window arithmetic.
 */
public class VelocityRule extends AbstractFraudRule {

    public static final String RULE_NAME = "VELOCITY_RULE";
    private static final int DEFAULT_MAX_TX_PER_HOUR = 5;

    public VelocityRule() {
        super(RULE_NAME, "Detects rapid-fire transactions exceeding maximum hourly velocity threshold");
    }

    @Override
    public double getWeight(DetectionConfig config) {
        return config != null && config.getVelocityWeight() != null ? config.getVelocityWeight() : 0.30;
    }

    @Override
    protected RuleEvaluationResult doEvaluate(Transaction transaction, RuleContext context) {
        Queue<LocalDateTime> recentQueue = context.getRecentTransactionTimestamps();
        int maxAllowed = context.getConfig().getMaxTxPerHour() > 0
                ? context.getConfig().getMaxTxPerHour()
                : DEFAULT_MAX_TX_PER_HOUR;

        // Current transaction adds 1 to the recent count
        int recentCount = recentQueue.size() + 1;
        double velocityRisk = clamp((double) recentCount / maxAllowed);

        if (recentCount >= maxAllowed) {
            String reason = "High transaction frequency (" + recentCount + " in the last hour; limit is " + maxAllowed + ")";
            return RuleEvaluationResult.flag(RULE_NAME, velocityRisk, reason);
        }

        return RuleEvaluationResult.pass(RULE_NAME);
    }
}
