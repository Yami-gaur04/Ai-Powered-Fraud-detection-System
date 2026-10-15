package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.math.BigDecimal;

/**
 * Evaluates whether transaction amount exceeds maximum threshold or severely deviates
 * from the cardholder's historical baseline spending.
 * Demonstrates inheritance and method overloading.
 */
public class HighAmountRule extends AbstractFraudRule {

    public static final String RULE_NAME = "HIGH_AMOUNT_RULE";
    private static final double DEFAULT_LIMIT = 50000.0;

    public HighAmountRule() {
        super(RULE_NAME, "Detects transaction amounts exceeding absolute or relative historical spending limits");
    }

    @Override
    public double getWeight(DetectionConfig config) {
        return config != null && config.getAmountWeight() != null ? config.getAmountWeight() : 0.40;
    }

    @Override
    protected RuleEvaluationResult doEvaluate(Transaction transaction, RuleContext context) {
        BigDecimal amount = transaction.getAmount();
        double amountVal = amount.doubleValue();

        BigDecimal configLimit = context.getConfig().getHighAmountLimit();
        double limitVal = configLimit != null ? configLimit.doubleValue() : DEFAULT_LIMIT;

        double amountRisk = calculateAmountRisk(amountVal, limitVal);
        boolean triggered = false;
        StringBuilder reason = new StringBuilder();

        if (amountVal > limitVal) {
            triggered = true;
            reason.append("Amount exceeds the high-value limit of ₹").append(String.format("%,.2f", limitVal));
        }

        Double avgSpend = context.getHistoricalAverageSpend();
        if (avgSpend != null && avgSpend > 0.0 && amountVal > 3.0 * avgSpend) {
            triggered = true;
            amountRisk = Math.max(amountRisk, 0.85);
            if (reason.length() > 0) {
                reason.append("; ");
            }
            reason.append("Amount is more than 3x the user's average spend of ₹").append(String.format("%,.2f", avgSpend));
        }

        return triggered
                ? RuleEvaluationResult.flag(RULE_NAME, amountRisk, reason.toString())
                : RuleEvaluationResult.pass(RULE_NAME);
    }

    /**
     * Overloaded method: Calculates amount risk ratio given both amount and explicit limit.
     *
     * @param amount transaction amount
     * @param limit  threshold limit
     * @return risk score clamped between 0.0 and 1.0
     */
    public double calculateAmountRisk(double amount, double limit) {
        if (limit <= 0.0) return 1.0;
        return clamp(amount / limit);
    }

    /**
     * Overloaded method: Calculates amount risk ratio against default system limit.
     * Demonstrates method overloading in concrete classes.
     *
     * @param amount transaction amount
     * @return risk score clamped between 0.0 and 1.0
     */
    public double calculateAmountRisk(double amount) {
        return calculateAmountRisk(amount, DEFAULT_LIMIT);
    }
}
