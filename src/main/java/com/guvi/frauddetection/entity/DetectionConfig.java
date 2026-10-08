package com.guvi.frauddetection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Single-row table (id = 1) that holds the admin-editable detection settings. */
@Entity
@Table(name = "detection_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetectionConfig {
    @Id
    private Long id = 1L;

    private Double flagThreshold;        // score >= this  -> FLAGGED
    private Double blockThreshold;       // score >= this  -> BLOCKED
    private BigDecimal highAmountLimit;  // amounts near/above this are risky
    private Integer maxTxPerHour;        // velocity rule
    private Double amountWeight;
    private Double velocityWeight;
    private Double locationWeight;
    private Double deviceWeight;
}
