package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.DetectionReport;
import com.guvi.frauddetection.entity.AlgorithmVersion;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.repository.TransactionRepository;
import com.guvi.frauddetection.repository.UserRepository;
import com.guvi.frauddetection.service.AlgorithmService;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Admin dashboard web controller.
 * Calls existing backend services; no business logic duplicated here.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ReportService reportService;
    private final FraudCaseService fraudCaseService;
    private final AlgorithmService algorithmService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Admin dashboard: shows KPI cards and summary charts.
     * GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        DetectionReport report = reportService.buildReport();
        List<FraudCaseResponse> recentCases = fraudCaseService.getCases(null)
                .stream().limit(5).toList();

        // KPI data
        model.addAttribute("report", report);
        model.addAttribute("recentCases", recentCases);
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("pageSubtitle", "System overview and fraud detection metrics");
        model.addAttribute("currentUser", auth.getName());
        model.addAttribute("activeAlgorithm",
                algorithmService.listVersions().stream().filter(AlgorithmVersion::getActive).findFirst().orElse(null));
        return "admin/dashboard";
    }
}
