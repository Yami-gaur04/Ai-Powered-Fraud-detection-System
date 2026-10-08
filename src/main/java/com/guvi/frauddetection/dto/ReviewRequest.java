package com.guvi.frauddetection.dto;

import com.guvi.frauddetection.entity.CaseStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull CaseStatus status,   // CONFIRMED_FRAUD or FALSE_POSITIVE
        String feedback) {
}
