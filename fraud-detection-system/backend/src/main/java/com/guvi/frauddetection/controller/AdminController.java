package com.guvi.frauddetection.controller;

import com.guvi.frauddetection.dto.*;
import com.guvi.frauddetection.entity.AlgorithmVersion;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.service.AlgorithmService;
import com.guvi.frauddetection.service.ConfigService;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin side: System Configuration, Detection Monitoring, Algorithm Updates. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ConfigService configService;
    private final FraudCaseService fraudCaseService;
    private final AlgorithmService algorithmService;
    private final ReportService reportService;

    public AdminController(ConfigService configService,
                           FraudCaseService fraudCaseService,
                           AlgorithmService algorithmService,
                           ReportService reportService) {
        this.configService = configService;
        this.fraudCaseService = fraudCaseService;
        this.algorithmService = algorithmService;
        this.reportService = reportService;
    }

    // ---- System Configuration ----
    @GetMapping("/config")
    public DetectionConfig getConfig() {
        return configService.getConfig();
    }

    @PutMapping("/config")
    public DetectionConfig updateConfig(@Valid @RequestBody ConfigRequest req) {
        return configService.updateConfig(req);
    }

    // ---- Detection Monitoring ----
    @GetMapping("/fraud-cases")
    public List<FraudCaseResponse> cases(@RequestParam(required = false) CaseStatus status) {
        return fraudCaseService.getCases(status);
    }

    @GetMapping("/fraud-cases/{id}")
    public FraudCaseResponse oneCase(@PathVariable Long id) {
        return fraudCaseService.getCase(id);
    }

    @PutMapping("/fraud-cases/{id}/review")
    public FraudCaseResponse review(@PathVariable Long id, @Valid @RequestBody ReviewRequest req) {
        return fraudCaseService.review(id, req);
    }

    @GetMapping("/reports")
    public DetectionReport report() {
        return reportService.buildReport();
    }

    // ---- Algorithm Updates ----
    @GetMapping("/algorithms")
    public List<AlgorithmVersion> algorithms() {
        return algorithmService.listVersions();
    }

    @PostMapping("/algorithms/update")
    public AlgorithmVersion updateAlgorithm(@RequestBody(required = false) AlgorithmUpdateRequest req) {
        return algorithmService.updateAlgorithm(req);
    }
}
