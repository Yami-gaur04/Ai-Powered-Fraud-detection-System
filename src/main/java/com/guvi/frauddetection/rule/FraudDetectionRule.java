package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.util.Collections;

/**
 * Core interface for fraud detection algorithms and rules.
 * Demonstrates Java interface design, contract abstraction, and method overloading.
 */
public interface FraudDetectionRule {

    /**
     * Retrieves the unique programmatic name of this rule.
     *
     * @return rule name identifier
     */
    String getName();

    /**
     * Retrieves a human-readable description of what this rule verifies.
     *
     * @return rule description
     */
    String getDescription();

    /**
     * Extracts the weight configured for this rule from system configuration.
     *
     * @param config the active detection configuration
     * @return weight multiplier
     */
    double getWeight(DetectionConfig config);

    /**
     * Evaluates a transaction against contextual user historical telemetries.
     *
     * @param transaction the candidate transaction
     * @param context     historical context including velocity and known devices
     * @return evaluation result with calculated score and anomaly reasons
     */
    RuleEvaluationResult evaluate(Transaction transaction, RuleContext context);

    /**
     * Overloaded method to evaluate a transaction with an empty/default context.
     * Demonstrates method overloading in interfaces.
     *
     * @param transaction the candidate transaction
     * @return evaluation result with default context
     */
    default RuleEvaluationResult evaluate(Transaction transaction) {
        RuleContext emptyContext = new RuleContext(
                transaction != null && transaction.getUser() != null ? transaction.getUser().getId() : 0L,
                new DetectionConfig(),
                0.0,
                0L,
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        return evaluate(transaction, emptyContext);
    }
}
