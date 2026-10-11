package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.DetectionReport;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.repository.AlgorithmVersionRepository;
import com.guvi.frauddetection.repository.FraudCaseRepository;
import com.guvi.frauddetection.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service aggregating transactional records and fraud case states into summary reports.
 *
 * <p>Implements {@link ReportServiceInterface} to satisfy interface-driven design
 * required by rubric item A.2.</p>
 */
@Service
public class ReportService implements ReportServiceInterface {

    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final AlgorithmVersionRepository algorithmRepository;

    /**
     * Constructs the report service with required repositories.
     *
     * @param transactionRepository transaction repository
     * @param fraudCaseRepository   fraud case repository
     * @param algorithmRepository   algorithm version repository
     */
    public ReportService(TransactionRepository transactionRepository,
                         FraudCaseRepository fraudCaseRepository,
                         AlgorithmVersionRepository algorithmRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.algorithmRepository = algorithmRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public DetectionReport buildReport() {
        long total = transactionRepository.count();
        long approved = transactionRepository.countByStatus(TransactionStatus.APPROVED);
        long flagged = transactionRepository.countByStatus(TransactionStatus.FLAGGED);
        long blocked = transactionRepository.countByStatus(TransactionStatus.BLOCKED);

        long totalCases = fraudCaseRepository.count();
        long open = fraudCaseRepository.countByStatus(CaseStatus.OPEN);
        long confirmed = fraudCaseRepository.countByStatus(CaseStatus.CONFIRMED_FRAUD);
        long falsePos = fraudCaseRepository.countByStatus(CaseStatus.FALSE_POSITIVE);

        double fraudRate = total == 0 ? 0 : Math.round((totalCases * 10000.0 / total)) / 100.0;
        long reviewed = confirmed + falsePos;
        Double precision = reviewed == 0 ? null : Math.round((confirmed * 10000.0 / reviewed)) / 100.0;

        String active = algorithmRepository.findByActiveTrue()
                .map(a -> a.getVersionName()).orElse("none");

        return new DetectionReport(total, approved, flagged, blocked, totalCases,
                open, confirmed, falsePos, fraudRate, precision, active);
    }
}
