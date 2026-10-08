package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.service.FraudCaseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * CSV export controller for fraud case reports.
 * Implements the ExportStrategy concept: CSV format selected via query param
 * (extensible to JSON export).
 * GET /admin/reports/export?format=csv
 */
@Controller
@RequestMapping("/admin/reports/export")
@RequiredArgsConstructor
public class ExportController {

    private final FraudCaseService fraudCaseService;

    @GetMapping
    public void export(@RequestParam(defaultValue = "csv") String format,
                       @RequestParam(required = false) String status,
                       HttpServletResponse response,
                       Authentication auth) throws IOException {

        CaseStatus filterStatus = null;
        if (status != null && !status.isBlank()) {
            try { filterStatus = CaseStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        List<FraudCaseResponse> cases = fraudCaseService.getCases(filterStatus);

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"fraud-report.csv\"");
            try (PrintWriter writer = response.getWriter()) {
                // CSV header
                writer.println("Case ID,Transaction ID,User Email,Amount,Merchant,Location," +
                        "Fraud Score,Case Status,Transaction Status,Reason,Created At");
                // CSV rows
                for (FraudCaseResponse c : cases) {
                    writer.printf("%d,%d,%s,%s,%s,%s,%.4f,%s,%s,\"%s\",%s%n",
                            c.caseId(),
                            c.transactionId(),
                            esc(c.userEmail()),
                            c.amount(),
                            esc(c.merchant()),
                            esc(c.location()),
                            c.fraudScore() != null ? c.fraudScore() : 0.0,
                            c.caseStatus(),
                            c.transactionStatus(),
                            esc(c.reason()),
                            c.createdAt() != null ? c.createdAt().toString() : "");
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported format: " + format);
        }
    }

    /** Escape double-quotes in CSV values. */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}
