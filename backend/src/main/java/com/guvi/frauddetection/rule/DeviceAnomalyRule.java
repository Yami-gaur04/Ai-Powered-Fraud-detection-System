package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;

import java.util.Set;

/**
 * Evaluates whether a transaction originates from an unfamiliar hardware fingerprint or browser device ID.
 * Utilizes {@link Set} for fast membership verification.
 */
public class DeviceAnomalyRule extends AbstractFraudRule {

    public static final String RULE_NAME = "DEVICE_ANOMALY_RULE";

    public DeviceAnomalyRule() {
        super(RULE_NAME, "Detects device identifier deviation from cardholder's recognized devices");
    }

    @Override
    public double getWeight(DetectionConfig config) {
        return config != null && config.getDeviceWeight() != null ? config.getDeviceWeight() : 0.15;
    }

    @Override
    protected RuleEvaluationResult doEvaluate(Transaction transaction, RuleContext context) {
        if (!context.hasHistory()) {
            return RuleEvaluationResult.pass(RULE_NAME);
        }

        String deviceId = transaction.getDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            return RuleEvaluationResult.flag(RULE_NAME, 0.75, "Transaction initiated without a valid device signature");
        }

        Set<String> knownDevices = context.getKnownDevices();
        if (!knownDevices.contains(deviceId.trim())) {
            return RuleEvaluationResult.flag(RULE_NAME, 1.0, "Transaction from a new/unrecognized device signature");
        }

        return RuleEvaluationResult.pass(RULE_NAME);
    }
}
