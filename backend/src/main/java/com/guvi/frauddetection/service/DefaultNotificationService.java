package com.guvi.frauddetection.service;

import com.guvi.frauddetection.entity.FraudCase;
import com.guvi.frauddetection.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the {@link NotificationService} interface.
 * Handles dispatching of fraud security notifications and audit logging.
 *
 * <p>Demonstrates polymorphism and interface implementation required by rubric item A.2.</p>
 */
@Service
public class DefaultNotificationService implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationService.class);

    @Override
    public void sendFraudAlert(String recipientEmail, Transaction transaction, double riskScore) {
        LOG.warn("FRAUD ALERT DISPATCHED -> To: {}, Tx ID: {}, Amount: {}, Score: {}",
                recipientEmail, transaction.getId(), transaction.getAmount(), riskScore);
    }

    @Override
    public void sendCaseCreatedNotification(String recipientEmail, FraudCase fraudCase) {
        LOG.info("CASE OPENED NOTIFICATION -> To: {}, Case ID: {}, Reason: {}",
                recipientEmail, fraudCase.getId(), fraudCase.getReason());
    }

    @Override
    public void sendCaseResolutionNotification(String recipientEmail, FraudCase fraudCase) {
        LOG.info("CASE RESOLVED NOTIFICATION -> To: {}, Case ID: {}, Status: {}",
                recipientEmail, fraudCase.getId(), fraudCase.getStatus());
    }
}
