package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

/**
 * Composite Fraud Evaluation Engine demonstrating Runtime Polymorphism,
 * Method Overloading, and weighted multi-rule scoring aggregation.
 */
@Component
public class CompositeFraudEngine {

    private static final Logger log = LoggerFactory.getLogger(CompositeFraudEngine.class);

    private final List<FraudDetectionRule> defaultRules;

    /**
     * Initializes the engine with the standard rule set.
     */
    public CompositeFraudEngine() {
        this.defaultRules = List.of(
                new HighAmountRule(),
                new VelocityRule(),
                new LocationAnomalyRule(),
                new DeviceAnomalyRule(),
                new OddHoursRule()
        );
    }

    /**
     * Constructs the engine with a custom injected list of rules.
     *
     * @param rules list of rules implementing {@link FraudDetectionRule}
     */
    public CompositeFraudEngine(List<FraudDetectionRule> rules) {
        this.defaultRules = rules != null ? List.copyOf(rules) : Collections.emptyList();
    }

    /**
     * Evaluates a transaction using the default registered rules.
     * Demonstrates runtime polymorphism over the {@link FraudDetectionRule} interface.
     *
     * @param transaction the candidate transaction
     * @param context     contextual user telemetry
     * @return composite evaluation result
     */
    public CompositeEvaluationResult evaluate(Transaction transaction, RuleContext context) {
        return evaluate(transaction, context, this.defaultRules);
    }

    /**
     * Overloaded evaluation method permitting custom dynamic rule sets.
     * Demonstrates method overloading and polymorphic loop dispatch.
     *
     * @param transaction candidate transaction
     * @param context     contextual history
     * @param activeRules specific list of rules to execute
     * @return composite evaluation result
     */
    public CompositeEvaluationResult evaluate(Transaction transaction,
                                             RuleContext context,
                                             List<FraudDetectionRule> activeRules) {
        DetectionConfig config = context.getConfig();
        List<String> reasons = new ArrayList<>();
        Map<String, Double> ruleScores = new HashMap<>();

        double weightedScoreSum = 0.0;
        double totalWeight = 0.0;

        // Polymorphic loop executing each rule through the FraudDetectionRule interface
        for (FraudDetectionRule rule : activeRules) {
            RuleEvaluationResult result = rule.evaluate(transaction, context);
            double weight = rule.getWeight(config);

            ruleScores.put(rule.getName(), result.getRiskScore());
            weightedScoreSum += result.getRiskScore() * weight;
            totalWeight += weight;

            if (result.isTriggered() && result.getReason() != null) {
                reasons.add(result.getReason());
            }

            log.debug("Rule [{}] -> Score: {}, Triggered: {}", rule.getName(), result.getRiskScore(), result.isTriggered());
        }

        double normalizedScore = totalWeight > 0.0 ? (weightedScoreSum / totalWeight) : 0.0;
        // Round to 4 decimal places
        normalizedScore = Math.round(normalizedScore * 10000.0) / 10000.0;

        TransactionStatus status;
        if (normalizedScore >= config.getBlockThreshold()) {
            status = TransactionStatus.BLOCKED;
        } else if (normalizedScore >= config.getFlagThreshold()) {
            status = TransactionStatus.FLAGGED;
        } else {
            status = TransactionStatus.APPROVED;
        }

        return new CompositeEvaluationResult(normalizedScore, status, reasons, ruleScores);
    }

    /**
     * Strongly-typed evaluation result container.
     */
    public static class CompositeEvaluationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private final double overallScore;
        private final TransactionStatus status;
        private final List<String> reasons;
        private final Map<String, Double> individualScores;

        public CompositeEvaluationResult(double overallScore,
                                         TransactionStatus status,
                                         List<String> reasons,
                                         Map<String, Double> individualScores) {
            this.overallScore = overallScore;
            this.status = status;
            this.reasons = reasons != null ? Collections.unmodifiableList(reasons) : Collections.emptyList();
            this.individualScores = individualScores != null ? Collections.unmodifiableMap(individualScores) : Collections.emptyMap();
        }

        public double getOverallScore() {
            return overallScore;
        }

        public TransactionStatus getStatus() {
            return status;
        }

        public List<String> getReasons() {
            return reasons;
        }

        public Map<String, Double> getIndividualScores() {
            return individualScores;
        }
    }
}
