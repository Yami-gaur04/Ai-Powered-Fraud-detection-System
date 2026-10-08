package com.guvi.frauddetection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
}
