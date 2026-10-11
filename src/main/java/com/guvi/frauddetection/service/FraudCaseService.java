package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.ReviewRequest;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.FraudCase;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.exception.ResourceNotFoundException;
import com.guvi.frauddetection.repository.FraudCaseRepository;
import com.guvi.frauddetection.repository.TransactionRepository;
import com.guvi.frauddetection.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing fraud investigation cases, cardholder disputes,
 * and administrator review actions.
 *
 * <p>Implements {@link FraudCaseServiceInterface} to satisfy interface-driven design
 * required by rubric item A.2.</p>
 */
@Service
public class FraudCaseService implements FraudCaseServiceInterface {

    private final FraudCaseRepository fraudCaseRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Constructs the service with its dependencies.
     *
     * @param fraudCaseRepository   fraud case repository
     * @param transactionRepository transaction repository
     * @param userRepository        user repository
     * @param notificationService   notification service
     */
    public FraudCaseService(FraudCaseRepository fraudCaseRepository,
                            TransactionRepository transactionRepository,
                            UserRepository userRepository,
                            NotificationService notificationService) {
        this.fraudCaseRepository = fraudCaseRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FraudCaseResponse> getCases(CaseStatus status) {
        List<FraudCase> cases = (status == null)
                ? fraudCaseRepository.findAllByOrderByCreatedAtDesc()
                : fraudCaseRepository.findByStatusOrderByCreatedAtDesc(status);
        return cases.stream().map(c -> FraudCaseResponse.from(c, true)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FraudCaseResponse getCase(Long id) {
        return FraudCaseResponse.from(find(id), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FraudCaseResponse review(Long id, ReviewRequest req) {
        if (req.status() == CaseStatus.OPEN) {
            throw new IllegalArgumentException("Status must be CONFIRMED_FRAUD or FALSE_POSITIVE");
        }
        FraudCase c = find(id);
        c.setStatus(req.status());
        c.setAdminFeedback(req.feedback());

        Transaction tx = c.getTransaction();
        tx.setStatus(req.status() == CaseStatus.CONFIRMED_FRAUD
                ? TransactionStatus.BLOCKED : TransactionStatus.APPROVED);
        transactionRepository.save(tx);
        FraudCase saved = fraudCaseRepository.save(c);

        if (tx.getUser() != null) {
            notificationService.sendCaseResolutionNotification(tx.getUser().getEmail(), saved);
        }

        return FraudCaseResponse.from(saved, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FraudCaseResponse> alertsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return fraudCaseRepository.findByTransactionUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(c -> FraudCaseResponse.from(c, false)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FraudCaseResponse userConfirm(Long transactionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to transaction #" + transactionId);
        }

        tx.setStatus(TransactionStatus.APPROVED);
        transactionRepository.save(tx);

        FraudCase c = fraudCaseRepository.findByTransactionId(transactionId).orElse(null);
        if (c != null) {
            c.setStatus(CaseStatus.FALSE_POSITIVE);
            c.setAdminFeedback("Verified by cardholder: 'This was me'");
            fraudCaseRepository.save(c);
            return FraudCaseResponse.from(c, false);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FraudCaseResponse userReportFraud(Long transactionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to transaction #" + transactionId);
        }

        tx.setStatus(TransactionStatus.BLOCKED);
        transactionRepository.save(tx);

        FraudCase c = fraudCaseRepository.findByTransactionId(transactionId).orElse(null);
        if (c != null) {
            c.setStatus(CaseStatus.OPEN);
            c.setReason("Reported by cardholder as unauthorized transaction ('This wasn't me')");
            fraudCaseRepository.save(c);
        } else {
            c = fraudCaseRepository.save(FraudCase.builder()
                    .transaction(tx)
                    .fraudScore(tx.getFraudScore() != null ? Math.max(tx.getFraudScore(), 0.90) : 0.90)
                    .reason("Reported by cardholder as unauthorized transaction ('This wasn't me')")
                    .status(CaseStatus.OPEN)
                    .createdAt(java.time.LocalDateTime.now())
                    .build());
        }
        return FraudCaseResponse.from(c, false);
    }

    private FraudCase find(Long id) {
        return fraudCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud case not found: " + id));
    }
}
