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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FraudCaseService {

    private final FraudCaseRepository fraudCaseRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public FraudCaseService(FraudCaseRepository fraudCaseRepository,
                            TransactionRepository transactionRepository,
                            UserRepository userRepository) {
        this.fraudCaseRepository = fraudCaseRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /** Admin: all cases, optionally filtered by status. */
    @Transactional(readOnly = true)
    public List<FraudCaseResponse> getCases(CaseStatus status) {
        List<FraudCase> cases = (status == null)
                ? fraudCaseRepository.findAllByOrderByCreatedAtDesc()
                : fraudCaseRepository.findByStatusOrderByCreatedAtDesc(status);
        return cases.stream().map(c -> FraudCaseResponse.from(c, true)).toList();
    }

    @Transactional(readOnly = true)
    public FraudCaseResponse getCase(Long id) {
        return FraudCaseResponse.from(find(id), true);
    }

    /** Admin reviews a case; this feedback is what the algorithm update learns from. */
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
        fraudCaseRepository.save(c);
        return FraudCaseResponse.from(c, true);
    }

    /** User: alerts for their own suspicious transactions. */
    @Transactional(readOnly = true)
    public List<FraudCaseResponse> alertsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return fraudCaseRepository.findByTransactionUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(c -> FraudCaseResponse.from(c, false)).toList();
    }

    private FraudCase find(Long id) {
        return fraudCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud case not found: " + id));
    }
}
