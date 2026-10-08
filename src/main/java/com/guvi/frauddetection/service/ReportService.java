package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.DetectionReport;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.repository.AlgorithmVersionRepository;
import com.guvi.frauddetection.repository.FraudCaseRepository;
import com.guvi.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final AlgorithmVersionRepository algorithmRepository;

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
