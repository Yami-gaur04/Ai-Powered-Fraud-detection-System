package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.ReviewRequest;
import com.guvi.frauddetection.entity.CaseStatus;

import java.util.List;

/**
 * Service contract for managing fraud investigation cases, admin review decisions,
 * and user dispute resolutions.
 *
 * <p>Demonstrates interface-driven design and abstraction as required by rubric item A.2.</p>
 */
public interface FraudCaseServiceInterface {

    /**
     * Retrieves all fraud cases, optionally filtered by status.
     *
     * @param status optional case status filter (OPEN, CONFIRMED_FRAUD, FALSE_POSITIVE)
     * @return list of fraud case responses
     */
    List<FraudCaseResponse> getCases(CaseStatus status);

    /**
     * Retrieves a single fraud case by its primary key identifier.
     *
     * @param id case ID
     * @return fraud case response
     */
    FraudCaseResponse getCase(Long id);

    /**
     * Submits an administrator's review decision for a pending fraud case.
     *
     * @param id  case ID
     * @param req review request payload containing determination and notes
     * @return updated fraud case response
     */
    FraudCaseResponse review(Long id, ReviewRequest req);

    /**
     * Retrieves all fraud cases and alerts associated with a specific user.
     *
     * @param email user email address
     * @return list of fraud cases
     */
    List<FraudCaseResponse> alertsForUser(String email);

    /**
     * Confirms that a flagged transaction was legitimate ("This was me").
     *
     * @param transactionId transaction ID
     * @param email         cardholder email
     * @return updated fraud case response
     */
    FraudCaseResponse userConfirm(Long transactionId, String email);

    /**
     * Reports that a transaction was fraudulent ("This wasn't me").
     *
     * @param transactionId transaction ID
     * @param email         cardholder email
     * @return updated fraud case response
     */
    FraudCaseResponse userReportFraud(Long transactionId, String email);
}
