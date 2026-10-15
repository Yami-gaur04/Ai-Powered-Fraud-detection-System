package com.guvi.frauddetection.rule;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulates the output of evaluating a single fraud detection rule.
 */
public class RuleEvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String ruleName;
    private final double riskScore;
    private final boolean triggered;
    private final String reason;

    /**
     * Constructs a RuleEvaluationResult.
     *
     * @param ruleName  identifier name of the rule
     * @param riskScore calculated risk score normalized between 0.0 and 1.0
     * @param triggered whether this rule detected an anomaly
     * @param reason    descriptive explanation if anomaly triggered, or null
     */
    public RuleEvaluationResult(String ruleName, double riskScore, boolean triggered, String reason) {
        this.ruleName = Objects.requireNonNull(ruleName, "ruleName must not be null");
        this.riskScore = Math.max(0.0, Math.min(1.0, riskScore));
        this.triggered = triggered;
        this.reason = reason;
    }

    public static RuleEvaluationResult pass(String ruleName) {
        return new RuleEvaluationResult(ruleName, 0.0, false, null);
    }

    public static RuleEvaluationResult flag(String ruleName, double riskScore, String reason) {
        return new RuleEvaluationResult(ruleName, riskScore, true, reason);
    }

    public String getRuleName() {
        return ruleName;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "RuleEvaluationResult{" +
                "ruleName='" + ruleName + '\'' +
                ", riskScore=" + riskScore +
                ", triggered=" + triggered +
                ", reason='" + reason + '\'' +
                '}';
    }
}
