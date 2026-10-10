package com.guvi.frauddetection.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConfigRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double flagThreshold,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double blockThreshold,
        @NotNull @DecimalMin("1.0") BigDecimal highAmountLimit,
        @NotNull @Min(1) Integer maxTxPerHour,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double amountWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double velocityWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double locationWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double deviceWeight) {
}
