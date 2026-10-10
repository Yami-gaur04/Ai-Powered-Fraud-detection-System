package com.guvi.frauddetection.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Single-row table (id = 1) that holds the admin-editable detection settings. */
@Entity
@Table(name = "detection_config")
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

    public DetectionConfig() {}

    public DetectionConfig(Long id, Double flagThreshold, Double blockThreshold, BigDecimal highAmountLimit,
                           Integer maxTxPerHour, Double amountWeight, Double velocityWeight,
                           Double locationWeight, Double deviceWeight) {
        this.id = id;
        this.flagThreshold = flagThreshold;
        this.blockThreshold = blockThreshold;
        this.highAmountLimit = highAmountLimit;
        this.maxTxPerHour = maxTxPerHour;
        this.amountWeight = amountWeight;
        this.velocityWeight = velocityWeight;
        this.locationWeight = locationWeight;
        this.deviceWeight = deviceWeight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getFlagThreshold() { return flagThreshold; }
    public void setFlagThreshold(Double flagThreshold) { this.flagThreshold = flagThreshold; }

    public Double getBlockThreshold() { return blockThreshold; }
    public void setBlockThreshold(Double blockThreshold) { this.blockThreshold = blockThreshold; }

    public BigDecimal getHighAmountLimit() { return highAmountLimit; }
    public void setHighAmountLimit(BigDecimal highAmountLimit) { this.highAmountLimit = highAmountLimit; }

    public Integer getMaxTxPerHour() { return maxTxPerHour; }
    public void setMaxTxPerHour(Integer maxTxPerHour) { this.maxTxPerHour = maxTxPerHour; }

    public Double getAmountWeight() { return amountWeight; }
    public void setAmountWeight(Double amountWeight) { this.amountWeight = amountWeight; }

    public Double getVelocityWeight() { return velocityWeight; }
    public void setVelocityWeight(Double velocityWeight) { this.velocityWeight = velocityWeight; }

    public Double getLocationWeight() { return locationWeight; }
    public void setLocationWeight(Double locationWeight) { this.locationWeight = locationWeight; }

    public Double getDeviceWeight() { return deviceWeight; }
    public void setDeviceWeight(Double deviceWeight) { this.deviceWeight = deviceWeight; }
}
