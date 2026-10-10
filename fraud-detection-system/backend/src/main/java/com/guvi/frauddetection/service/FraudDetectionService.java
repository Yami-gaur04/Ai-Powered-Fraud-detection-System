package com.guvi.frauddetection.service;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted rule-based anomaly scoring. Each factor produces a risk between 0 and 1,
 * the final score is the weighted average (0 to 1). Thresholds come from DetectionConfig.
 *
 * To plug in a real ML model later, replace the body of evaluate() (for example call
 * a Python model service over HTTP) and keep returning a Result.
 */
@Service
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final ConfigService configService;

    public FraudDetectionService(TransactionRepository transactionRepository,
                                 ConfigService configService) {
        this.transactionRepository = transactionRepository;
        this.configService = configService;
    }

    public record Result(double score, TransactionStatus status, List<String> reasons) {}

    /** Call BEFORE saving the new transaction so it is not counted in its own history. */
    public Result evaluate(User user, Transaction tx) {
        DetectionConfig c = configService.getConfig();
        List<String> reasons = new ArrayList<>();
        Long userId = user.getId();
        boolean hasHistory = transactionRepository.countByUserId(userId) > 0;

        // 1) Amount risk
        double amount = tx.getAmount().doubleValue();
        double limit = c.getHighAmountLimit().doubleValue();
        double amountRisk = Math.min(1.0, amount / limit);
        if (amount > limit) {
            reasons.add("Amount exceeds the high-value limit");
        }
        Double avg = transactionRepository.findAverageAmountByUserId(userId);
        if (avg != null && avg > 0 && amount > 3 * avg) {
            amountRisk = Math.max(amountRisk, 0.8);
            reasons.add("Amount is more than 3x the user's average spend");
        }

        // 2) Velocity risk (transactions in the last hour, including this one)
        long recent = transactionRepository.countByUserIdAndTransactionTimeAfter(
                userId, tx.getTransactionTime().minusHours(1)) + 1;
        double velocityRisk = Math.min(1.0, (double) recent / c.getMaxTxPerHour());
        if (recent >= c.getMaxTxPerHour()) {
            reasons.add("High transaction frequency (" + recent + " in the last hour)");
        }

        // 3) New location / 4) new device (only meaningful once the user has history)
        double locationRisk = 0;
        double deviceRisk = 0;
        if (hasHistory) {
            if (!transactionRepository.existsByUserIdAndLocationIgnoreCase(userId, tx.getLocation())) {
                locationRisk = 1;
                reasons.add("Transaction from a new location");
            }
            if (!transactionRepository.existsByUserIdAndDeviceId(userId, tx.getDeviceId())) {
                deviceRisk = 1;
                reasons.add("Transaction from a new device");
            }
        }

        double wA = nz(c.getAmountWeight());
        double wV = nz(c.getVelocityWeight());
        double wL = nz(c.getLocationWeight());
        double wD = nz(c.getDeviceWeight());
        double totalWeight = wA + wV + wL + wD;

        double score = totalWeight == 0 ? 0
                : (amountRisk * wA + velocityRisk * wV + locationRisk * wL + deviceRisk * wD) / totalWeight;
        score = Math.round(score * 10000.0) / 10000.0;

        TransactionStatus status;
        if (score >= c.getBlockThreshold()) {
            status = TransactionStatus.BLOCKED;
        } else if (score >= c.getFlagThreshold()) {
            status = TransactionStatus.FLAGGED;
        } else {
            status = TransactionStatus.APPROVED;
        }
        return new Result(score, status, reasons);
    }

    private double nz(Double d) {
        return d == null ? 0 : d;
    }
}
