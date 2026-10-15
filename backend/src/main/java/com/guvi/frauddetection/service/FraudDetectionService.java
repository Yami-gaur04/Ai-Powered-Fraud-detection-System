package com.guvi.frauddetection.service;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.repository.TransactionRepository;
import com.guvi.frauddetection.rule.CompositeFraudEngine;
import com.guvi.frauddetection.rule.CompositeFraudEngine.CompositeEvaluationResult;
import com.guvi.frauddetection.rule.RuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * High-level orchestration service for fraud detection evaluation.
 * Constructs telemetry {@link RuleContext} from transaction repositories
 * and delegates to {@link CompositeFraudEngine} for polymorphic rule execution.
 */
@Service
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final TransactionRepository transactionRepository;
    private final ConfigService configService;
    private final CompositeFraudEngine compositeFraudEngine;

    public FraudDetectionService(TransactionRepository transactionRepository,
                                 ConfigService configService,
                                 CompositeFraudEngine compositeFraudEngine) {
        this.transactionRepository = transactionRepository;
        this.configService = configService;
        this.compositeFraudEngine = compositeFraudEngine;
    }

    /**
     * Backward-compatible evaluation result record.
     */
    public record Result(double score, TransactionStatus status, List<String> reasons) {}

    /**
     * Evaluates a candidate transaction against historical user patterns and active thresholds.
     * Must be invoked BEFORE persisting the new transaction to preserve history boundary.
     *
     * @param user cardholder entity
     * @param tx   candidate transaction
     * @return {@link Result} containing calculated score, decision status, and anomaly reasons
     */
    public Result evaluate(User user, Transaction tx) {
        DetectionConfig config = configService.getConfig();
        Long userId = user != null ? user.getId() : 0L;

        // Fetch historical transactions
        List<Transaction> history = (userId != null && userId > 0)
                ? transactionRepository.findByUserIdOrderByTransactionTimeDesc(userId)
                : Collections.emptyList();

        long historyCount = history.size();
        Double avgSpend = transactionRepository.findAverageAmountByUserId(userId);

        Set<String> knownLocations = history.stream()
                .map(Transaction::getLocation)
                .filter(l -> l != null && !l.isBlank())
                .collect(Collectors.toSet());

        Set<String> knownDevices = history.stream()
                .map(Transaction::getDeviceId)
                .filter(d -> d != null && !d.isBlank())
                .collect(Collectors.toSet());

        LocalDateTime oneHourAgo = (tx.getTransactionTime() != null ? tx.getTransactionTime() : LocalDateTime.now()).minusHours(1);
        List<LocalDateTime> recentTimes = history.stream()
                .map(Transaction::getTransactionTime)
                .filter(t -> t != null && t.isAfter(oneHourAgo))
                .toList();

        RuleContext context = new RuleContext(
                userId,
                config,
                avgSpend,
                historyCount,
                knownLocations,
                knownDevices,
                recentTimes,
                history
        );

        CompositeEvaluationResult compResult = compositeFraudEngine.evaluate(tx, context);
        log.info("Fraud evaluation complete for user {}: score={}, status={}, reasons={}",
                userId, compResult.getOverallScore(), compResult.getStatus(), compResult.getReasons());

        return new Result(compResult.getOverallScore(), compResult.getStatus(), compResult.getReasons());
    }
}
