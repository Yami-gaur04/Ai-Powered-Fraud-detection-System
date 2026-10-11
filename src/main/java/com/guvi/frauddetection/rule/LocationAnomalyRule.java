package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.util.Set;

/**
 * Evaluates whether a transaction originates from an unfamiliar geographic location.
 * Utilizes {@link Set} (HashSet) for O(1) membership lookups against cardholder history.
 */
public class LocationAnomalyRule extends AbstractFraudRule {

    public static final String RULE_NAME = "LOCATION_ANOMALY_RULE";

    public LocationAnomalyRule() {
        super(RULE_NAME, "Detects geographic deviation from previously verified transaction locations");
    }

    @Override
    public double getWeight(DetectionConfig config) {
        return config != null && config.getLocationWeight() != null ? config.getLocationWeight() : 0.15;
    }

    @Override
    protected RuleEvaluationResult doEvaluate(Transaction transaction, RuleContext context) {
        // Location anomaly is only meaningful once the user has a transaction history
        if (!context.hasHistory()) {
            return RuleEvaluationResult.pass(RULE_NAME);
        }

        String location = transaction.getLocation();
        if (location == null || location.isBlank()) {
            return RuleEvaluationResult.flag(RULE_NAME, 0.70, "Transaction location missing or unrecognized");
        }

        Set<String> knownLocations = context.getKnownLocations();
        boolean isKnown = knownLocations.stream()
                .anyMatch(loc -> loc.equalsIgnoreCase(location.trim()));

        if (!isKnown) {
            return RuleEvaluationResult.flag(RULE_NAME, 1.0, "Transaction from a new/unfamiliar location: '" + location + "'");
        }

        return RuleEvaluationResult.pass(RULE_NAME);
    }
}
