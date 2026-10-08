package com.guvi.frauddetection.repository;

import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionTimeDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndTransactionTimeAfter(Long userId, LocalDateTime since);

    boolean existsByUserIdAndLocationIgnoreCase(Long userId, String location);

    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);

    long countByStatus(TransactionStatus status);

    @Query("select avg(t.amount) from Transaction t where t.user.id = :userId")
    Double findAverageAmountByUserId(@Param("userId") Long userId);
}
