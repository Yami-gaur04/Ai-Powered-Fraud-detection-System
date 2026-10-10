package com.guvi.frauddetection.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_cases")
public class FraudCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private Double fraudScore;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    private CaseStatus status;

    @Column(length = 500)
    private String adminFeedback;

    private LocalDateTime createdAt;

    public FraudCase() {}

    public FraudCase(Long id, Transaction transaction, Double fraudScore, String reason,
                     CaseStatus status, String adminFeedback, LocalDateTime createdAt) {
        this.id = id;
        this.transaction = transaction;
        this.fraudScore = fraudScore;
        this.reason = reason;
        this.status = status;
        this.adminFeedback = adminFeedback;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Transaction transaction;
        private Double fraudScore;
        private String reason;
        private CaseStatus status;
        private String adminFeedback;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder transaction(Transaction transaction) { this.transaction = transaction; return this; }
        public Builder fraudScore(Double fraudScore) { this.fraudScore = fraudScore; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder status(CaseStatus status) { this.status = status; return this; }
        public Builder adminFeedback(String adminFeedback) { this.adminFeedback = adminFeedback; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FraudCase build() {
            return new FraudCase(id, transaction, fraudScore, reason, status, adminFeedback, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public Double getFraudScore() { return fraudScore; }
    public void setFraudScore(Double fraudScore) { this.fraudScore = fraudScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public String getAdminFeedback() { return adminFeedback; }
    public void setAdminFeedback(String adminFeedback) { this.adminFeedback = adminFeedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
