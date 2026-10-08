package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.AlgorithmUpdateRequest;
import com.guvi.frauddetection.entity.AlgorithmVersion;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.repository.AlgorithmVersionRepository;
import com.guvi.frauddetection.repository.FraudCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * "Algorithm Updates": refines detection using the performance data and feedback
 * the admin has given on reviewed cases, then stores a new algorithm version.
 */
@Service
@RequiredArgsConstructor
public class AlgorithmService {

    private final AlgorithmVersionRepository algorithmRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final ConfigService configService;

    @Transactional(readOnly = true)
    public List<AlgorithmVersion> listVersions() {
        return algorithmRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public AlgorithmVersion updateAlgorithm(AlgorithmUpdateRequest req) {
        long confirmed = fraudCaseRepository.countByStatus(CaseStatus.CONFIRMED_FRAUD);
        long falsePositives = fraudCaseRepository.countByStatus(CaseStatus.FALSE_POSITIVE);
        long reviewed = confirmed + falsePositives;
        if (reviewed == 0) {
            throw new IllegalArgumentException(
                    "No reviewed cases yet. Review some fraud cases first so the algorithm has feedback to learn from.");
        }

        double precision = (double) confirmed / reviewed;   // 0..1
        DetectionConfig c = configService.getConfig();
        double flag = c.getFlagThreshold();
        double block = c.getBlockThreshold();
        String action;

        if (precision < 0.5) {
            // too many false alarms -> be less sensitive
            flag = Math.min(flag + 0.05, block - 0.05);
            action = "Too many false positives: flag threshold raised";
        } else if (precision > 0.8) {
            // detections are reliable -> be a little more sensitive
            flag = Math.max(flag - 0.02, 0.20);
            action = "High precision: flag threshold lowered slightly";
        } else {
            action = "Precision acceptable: thresholds unchanged";
        }
        flag = Math.round(flag * 100.0) / 100.0;
        c.setFlagThreshold(flag);
        configService.save(c);

        algorithmRepository.findByActiveTrue().ifPresent(old -> {
            old.setActive(false);
            algorithmRepository.save(old);
        });

        long count = algorithmRepository.count();
        String name = (req != null && req.versionName() != null && !req.versionName().isBlank())
                ? req.versionName().trim()
                : "v" + (count + 1) + ".0-feedback-tuned";
        String userDesc = (req != null && req.description() != null && !req.description().isBlank())
                ? req.description().trim() + " | " : "";

        double precisionPct = Math.round(precision * 10000.0) / 100.0;
        return algorithmRepository.save(AlgorithmVersion.builder()
                .versionName(name)
                .description(userDesc + action + " (flag threshold now " + flag + ", based on "
                        + reviewed + " reviewed cases)")
                .accuracy(precisionPct)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
