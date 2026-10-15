package com.guvi.frauddetection.dto;

public record DetectionReport(
        long totalTransactions,
        long approved,
        long flagged,
        long blocked,
        long totalCases,
        long openCases,
        long confirmedFraud,
        long falsePositives,
        double fraudRatePercent,
        Double detectionPrecisionPercent,
        String activeAlgorithm) {
}
