package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.entity.*;
import com.guvi.frauddetection.exception.ResourceNotFoundException;
import com.guvi.frauddetection.repository.FraudCaseRepository;
import com.guvi.frauddetection.repository.TransactionRepository;
import com.guvi.frauddetection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService detectionService;

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
            fraudCaseRepository.save(FraudCase.builder()
                    .transaction(tx)
                    .fraudScore(result.score())
                    .reason(reason)
                    .status(CaseStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build());
            message = "ALERT: transaction " + result.status() + ". Reason: " + reason;
        }
        return TransactionResponse.from(tx, message);
    }

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

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
