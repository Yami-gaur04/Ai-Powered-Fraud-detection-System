package com.guvi.frauddetection.dto;

import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.FraudCase;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudCaseResponse(
        Long caseId,
        Long transactionId,
        String userEmail,
        BigDecimal amount,
        String merchant,
        String location,
        Double fraudScore,
        String reason,
        CaseStatus caseStatus,
        TransactionStatus transactionStatus,
        String adminFeedback,
        String message,
        LocalDateTime createdAt) {

    /** Must be called inside a transaction (touches lazy relations). */
    public static FraudCaseResponse from(FraudCase c, boolean forAdmin) {
        Transaction t = c.getTransaction();
        String message = "Suspicious transaction of " + t.getAmount() + " at " + t.getMerchant()
                + " was " + t.getStatus() + ". Reason: " + c.getReason();
        return new FraudCaseResponse(
                c.getId(), t.getId(),
                forAdmin ? t.getUser().getEmail() : null,
                t.getAmount(), t.getMerchant(), t.getLocation(),
                c.getFraudScore(), c.getReason(), c.getStatus(), t.getStatus(),
                forAdmin ? c.getAdminFeedback() : null,
                message, c.getCreatedAt());
    }
}
