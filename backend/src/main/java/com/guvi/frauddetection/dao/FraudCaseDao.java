package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.common.Repository;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.FraudCase;
import com.guvi.frauddetection.exception.DataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link FraudCase} entities.
 * Extends the generic {@link Repository} contract and adds
 * case-management query methods.
 *
 * @see JdbcFraudCaseDao
 */
public interface FraudCaseDao extends Repository<FraudCase, Long> {

    /**
     * Retrieves all fraud cases ordered by creation date descending.
     *
     * @return list of all fraud cases, newest first
     * @throws DataAccessException if a database error occurs
     */
    List<FraudCase> findAllOrderByCreatedAtDesc() throws DataAccessException;

    /**
     * Retrieves fraud cases filtered by status, ordered by creation date descending.
     *
     * @param status the case status to filter by
     * @return list of matching fraud cases
     * @throws DataAccessException if a database error occurs
     */
    List<FraudCase> findByStatus(CaseStatus status) throws DataAccessException;

    /**
     * Finds a fraud case associated with a specific transaction.
     *
     * @param transactionId the transaction's unique identifier
     * @return an {@link Optional} containing the fraud case if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<FraudCase> findByTransactionId(Long transactionId) throws DataAccessException;

    /**
     * Counts fraud cases with a specific status.
     *
     * @param status the case status to count
     * @return count of matching cases
     * @throws DataAccessException if a database error occurs
     */
    long countByStatus(CaseStatus status) throws DataAccessException;
}
