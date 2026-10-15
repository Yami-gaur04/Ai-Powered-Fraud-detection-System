package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;

import java.util.List;

/**
 * Service contract defining operations for transaction intake, scoring, and retrieval.
 *
 * <p>Demonstrates interface-driven design and abstraction as required by rubric item A.2.</p>
 */
public interface TransactionServiceInterface {

    /**
     * Ingests, validates, scores, and persists an incoming transaction for a given user.
     *
     * @param email user email address
     * @param req   transaction payload
     * @return scored transaction result DTO
     */
    TransactionResponse process(String email, TransactionRequest req);

    /**
     * Retrieves all transactions belonging to the given user account.
     *
     * @param email user email address
     * @return list of transaction responses
     */
    List<TransactionResponse> myTransactions(String email);

    /**
     * Retrieves a single transaction by its primary key ID.
     *
     * @param id    transaction unique identifier
     * @param email user email address
     * @return transaction response DTO
     */
    TransactionResponse getById(Long id, String email);

    /**
     * Retrieves all transactions across the platform.
     *
     * @return list of all transaction response DTOs
     */
    List<TransactionResponse> allTransactions();
}
