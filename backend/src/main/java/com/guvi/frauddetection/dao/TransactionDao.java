package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.common.Repository;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.exception.DataAccessException;

import java.util.List;

/**
 * Data Access Object interface for {@link Transaction} entities.
 * Extends the generic {@link Repository} contract and adds domain-specific
 * query methods for fraud detection workflows.
 *
 * <p>Implementations use explicit JDBC (PreparedStatement, ResultSet)
 * to demonstrate database integration as required by the rubric.</p>
 *
 * @see JdbcTransactionDao
 */
public interface TransactionDao extends Repository<Transaction, Long> {

    /**
     * Retrieves all transactions for a given user, ordered by transaction time descending.
     *
     * @param userId the user's unique identifier
     * @return list of transactions, most recent first
     * @throws DataAccessException if a database error occurs
     */
    List<Transaction> findByUserId(Long userId) throws DataAccessException;

    /**
     * Counts the total number of transactions for a given user.
     *
     * @param userId the user's unique identifier
     * @return transaction count
     * @throws DataAccessException if a database error occurs
     */
    long countByUserId(Long userId) throws DataAccessException;

    /**
     * Counts transactions with a specific status.
     *
     * @param status the transaction status to filter by
     * @return count of matching transactions
     * @throws DataAccessException if a database error occurs
     */
    long countByStatus(TransactionStatus status) throws DataAccessException;

    /**
     * Calculates the average transaction amount for a given user.
     *
     * @param userId the user's unique identifier
     * @return the average amount, or null if no transactions exist
     * @throws DataAccessException if a database error occurs
     */
    Double findAverageAmountByUserId(Long userId) throws DataAccessException;
}
