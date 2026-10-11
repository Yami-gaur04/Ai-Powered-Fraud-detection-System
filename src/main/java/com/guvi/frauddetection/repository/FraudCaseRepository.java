package com.guvi.frauddetection.repository;

import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.FraudCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long> {
    List<FraudCase> findAllByOrderByCreatedAtDesc();
    List<FraudCase> findByStatusOrderByCreatedAtDesc(CaseStatus status);
    List<FraudCase> findByTransactionUserIdOrderByCreatedAtDesc(Long userId);
    Optional<FraudCase> findByTransactionId(Long transactionId);
    long countByStatus(CaseStatus status);
}
