package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.exception.InvalidTransactionException;
import com.guvi.frauddetection.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Abstract base class for all fraud detection rules.
 * Implements the Template Method design pattern, provides shared input validation,
 * SLF4J logging, and enforces consistent error handling across concrete rule subclasses.
 */
public abstract class AbstractFraudRule implements FraudDetectionRule {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final String name;
    private final String description;

    /**
     * Constructs an abstract fraud rule.
     *
     * @param name        the unique rule identifier
     * @param description human-readable description
     */
    protected AbstractFraudRule(String name, String description) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Template method: validates candidate transaction integrity before delegating to subclass.
     *
     * @param transaction the candidate transaction
     * @param context     contextual history and thresholds
     * @return evaluation result
     * @throws InvalidTransactionException if transaction is null or has negative amount
     */
    @Override
    public final RuleEvaluationResult evaluate(Transaction transaction, RuleContext context) {
        if (transaction == null) {
            throw new InvalidTransactionException("Transaction candidate cannot be null for rule: " + name);
        }
        if (transaction.getAmount() == null || transaction.getAmount().signum() < 0) {
            throw new InvalidTransactionException("Transaction amount cannot be null or negative");
        }
        if (context == null) {
            throw new IllegalArgumentException("RuleContext cannot be null");
        }

        log.debug("Evaluating rule [{}] for transaction amount: {}", name, transaction.getAmount());
        return doEvaluate(transaction, context);
    }

    /**
     * Subclasses implement specific rule logic here.
     *
     * @param transaction validated transaction
     * @param context     rule execution context
     * @return rule evaluation result
     */
    protected abstract RuleEvaluationResult doEvaluate(Transaction transaction, RuleContext context);

    /**
     * Helper to safely clamp values between 0.0 and 1.0.
     *
     * @param value double value
     * @return clamped value
     */
    protected double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
