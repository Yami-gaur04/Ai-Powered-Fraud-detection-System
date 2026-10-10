package com.guvi.frauddetection.dto;

import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String merchant,
        String location,
        String deviceId,
        String paymentMethod,
        LocalDateTime transactionTime,
        Double fraudScore,
        TransactionStatus status,
        String message) {

    public static TransactionResponse from(Transaction t, String message) {
        return new TransactionResponse(t.getId(), t.getAmount(), t.getMerchant(), t.getLocation(),
                t.getDeviceId(), t.getPaymentMethod(), t.getTransactionTime(),
                t.getFraudScore(), t.getStatus(), message);
    }
}
