package com.guvi.frauddetection.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount,
        @NotBlank String merchant,
        @NotBlank String location,
        @NotBlank String deviceId,
        String paymentMethod) {
}
