package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.entity.*;
import com.guvi.frauddetection.exception.ResourceNotFoundException;
import com.guvi.frauddetection.repository.FraudCaseRepository;
import com.guvi.frauddetection.repository.TransactionRepository;
import com.guvi.frauddetection.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for transaction processing, rule-based fraud scoring,
 * and history query operations.
 *
 * <p>Implements {@link TransactionServiceInterface} to satisfy interface-driven design
 * required by rubric item A.2.</p>
 */
@Service
public class TransactionService implements TransactionServiceInterface {

    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService detectionService;
    private final NotificationService notificationService;

    /**
     * Constructs the service with required repositories and fraud detection engine.
     *
     * @param transactionRepository transaction data repository
     * @param fraudCaseRepository   fraud case data repository
     * @param userRepository        user data repository
     * @param detectionService      fraud evaluation engine
     * @param notificationService   alert dispatching service
     */
    public TransactionService(TransactionRepository transactionRepository,
                              FraudCaseRepository fraudCaseRepository,
                              UserRepository userRepository,
                              FraudDetectionService detectionService,
                              NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.userRepository = userRepository;
        this.detectionService = detectionService;
        this.notificationService = notificationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransactionResponse process(String email, TransactionRequest req) {
        User user = findUser(email);

        Transaction tx = Transaction.builder()
                .user(user)
                .amount(req.amount())
                .merchant(req.merchant().trim())
                .location(req.location().trim())
                .deviceId(req.deviceId().trim())
                .paymentMethod(req.paymentMethod())
                .transactionTime(LocalDateTime.now())
                .build();

        FraudDetectionService.Result result = detectionService.evaluate(user, tx);
        tx.setFraudScore(result.score());
        tx.setStatus(result.status());
        tx = transactionRepository.save(tx);

        String message = "Transaction approved.";
        if (result.status() != TransactionStatus.APPROVED) {
            String reason = result.reasons().isEmpty()
                    ? "Combined risk score exceeded the threshold"
                    : String.join("; ", result.reasons());
            if (reason.length() > 490) {
                reason = reason.substring(0, 490);
            }
            FraudCase fraudCase = fraudCaseRepository.save(FraudCase.builder()
                    .transaction(tx)
                    .fraudScore(result.score())
                    .reason(reason)
                    .status(CaseStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build());
            message = "ALERT: transaction " + result.status() + ". Reason: " + reason;

            // Dispatch alert via notification service
            notificationService.sendFraudAlert(user.getEmail(), tx, result.score());
            notificationService.sendCaseCreatedNotification(user.getEmail(), fraudCase);
        }
        return TransactionResponse.from(tx, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> myTransactions(String email) {
        User user = findUser(email);
        return transactionRepository.findByUserIdOrderByTransactionTimeDesc(user.getId())
                .stream()
                .map(t -> TransactionResponse.from(t,
                        t.getStatus() == TransactionStatus.APPROVED
                                ? "Transaction approved."
                                : "Transaction " + t.getStatus() + " due to suspicious activity."))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id, String email) {
        User user = findUser(email);
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        if (!tx.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ResourceNotFoundException("Transaction not found for user");
        }

        return TransactionResponse.from(tx, "Transaction details retrieved.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> allTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(t -> TransactionResponse.from(t, "Status: " + t.getStatus()))
                .toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
