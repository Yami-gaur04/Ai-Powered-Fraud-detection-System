package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.time.LocalDateTime;

/**
 * Evaluates whether a transaction takes place during unusual/high-risk night hours (e.g., 01:00 to 05:00).
 */
public class OddHoursRule extends AbstractFraudRule {

    public static final String RULE_NAME = "ODD_HOURS_RULE";

    public OddHoursRule() {
        super(RULE_NAME, "Detects off-peak, high-risk late-night transaction initiation");
    }

    @Override
    public double getWeight(DetectionConfig config) {
        // Minor contextual baseline weight
        return 0.10;
    }

    @Override
    protected RuleEvaluationResult doEvaluate(Transaction transaction, RuleContext context) {
        LocalDateTime time = transaction.getTransactionTime() != null
                ? transaction.getTransactionTime()
                : LocalDateTime.now();

        int hour = time.getHour();
        // High risk window: 01:00 to 04:59 AM
        if (hour >= 1 && hour < 5) {
            return RuleEvaluationResult.flag(
                    RULE_NAME,
                    0.60,
                    "Transaction initiated during unusual off-peak hours (" + String.format("%02d:00", hour) + " hrs)"
            );
        }

        return RuleEvaluationResult.pass(RULE_NAME);
    }
}
