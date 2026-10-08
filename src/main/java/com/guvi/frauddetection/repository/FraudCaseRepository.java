package com.guvi.frauddetection.repository;

import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.FraudCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long> {
    List<FraudCase> findAllByOrderByCreatedAtDesc();
    List<FraudCase> findByStatusOrderByCreatedAtDesc(CaseStatus status);
    List<FraudCase> findByTransactionUserIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(CaseStatus status);
}
