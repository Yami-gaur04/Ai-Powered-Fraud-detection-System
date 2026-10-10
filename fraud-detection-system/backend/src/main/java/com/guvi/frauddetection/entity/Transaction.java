package com.guvi.frauddetection.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String merchant;
    private String location;
    private String deviceId;
    private String paymentMethod;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    private Double fraudScore;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public Transaction() {}

    public Transaction(Long id, User user, BigDecimal amount, String merchant, String location,
                       String deviceId, String paymentMethod, LocalDateTime transactionTime,
                       Double fraudScore, TransactionStatus status) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.merchant = merchant;
        this.location = location;
        this.deviceId = deviceId;
        this.paymentMethod = paymentMethod;
        this.transactionTime = transactionTime;
        this.fraudScore = fraudScore;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private BigDecimal amount;
        private String merchant;
        private String location;
        private String deviceId;
        private String paymentMethod;
        private LocalDateTime transactionTime;
        private Double fraudScore;
        private TransactionStatus status;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder merchant(String merchant) { this.merchant = merchant; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public Builder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder transactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; return this; }
        public Builder fraudScore(Double fraudScore) { this.fraudScore = fraudScore; return this; }
        public Builder status(TransactionStatus status) { this.status = status; return this; }

        public Transaction build() {
            return new Transaction(id, user, amount, merchant, location, deviceId, paymentMethod, transactionTime, fraudScore, status);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }

    public Double getFraudScore() { return fraudScore; }
    public void setFraudScore(Double fraudScore) { this.fraudScore = fraudScore; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
}
