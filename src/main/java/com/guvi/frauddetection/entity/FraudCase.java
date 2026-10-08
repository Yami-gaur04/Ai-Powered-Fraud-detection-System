package com.guvi.frauddetection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
}
