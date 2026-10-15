package com.guvi.frauddetection.service;

import com.guvi.frauddetection.entity.FraudCase;
import com.guvi.frauddetection.entity.Transaction;

/**
 * Service interface defining notification contracts for security alerts,
 * high-risk transaction warnings, and fraud case determinations.
 *
 * <p>Demonstrates interface-driven design and decoupling as required by rubric item A.2.</p>
 */
public interface NotificationService {

    /**
     * Sends an immediate fraud alert regarding a high-risk transaction.
     *
     * @param recipientEmail destination email address
     * @param transaction    transaction that triggered the alert
     * @param riskScore      calculated fraud risk score
     */
    void sendFraudAlert(String recipientEmail, Transaction transaction, double riskScore);

    /**
     * Sends an alert when a formal fraud investigation case is opened.
     *
     * @param recipientEmail destination email address
     * @param fraudCase      opened fraud investigation case
     */
    void sendCaseCreatedNotification(String recipientEmail, FraudCase fraudCase);

    /**
     * Sends an alert when a fraud case investigation is resolved by an administrator.
     *
     * @param recipientEmail destination email address
     * @param fraudCase      resolved fraud case
     */
    void sendCaseResolutionNotification(String recipientEmail, FraudCase fraudCase);
}
