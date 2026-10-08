package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.entity.AlgorithmVersion;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.service.AlgorithmService;
import com.guvi.frauddetection.service.FraudCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON REST endpoints consumed by Chart.js in the Thymeleaf views.
 * Returns chart data for fraud trend, monthly cases, algorithm performance,
 * and status distribution.
 *
 * These are NOT protected by JWT — they use the web session.
 */
@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartDataController {

    private final FraudCaseService fraudCaseService;
    private final AlgorithmService algorithmService;

    /**
     * Fraud trend over last N months (area chart).
     * GET /api/charts/fraud-trend?months=6
     */
    @GetMapping("/fraud-trend")
    public ResponseEntity<Map<String, Object>> fraudTrend(
            @RequestParam(defaultValue = "6") int months,
            Authentication auth) {

        List<FraudCaseResponse> allCases = fraudCaseService.getCases(null);

        // Group by month using LinkedHashMap to preserve order
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        LinkedHashMap<String, Long> monthlyData = new LinkedHashMap<>();

        // Build last N months buckets
        LocalDateTime now = LocalDateTime.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            monthlyData.put(month.format(monthFmt), 0L);
        }

        // Fill actual data
        allCases.stream()
                .filter(c -> c.createdAt() != null && c.createdAt().isAfter(now.minusMonths(months)))
                .forEach(c -> {
                    String key = c.createdAt().format(monthFmt);
                    monthlyData.merge(key, 1L, Long::sum);
                });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", new ArrayList<>(monthlyData.keySet()));
        result.put("values", new ArrayList<>(monthlyData.values()));
        return ResponseEntity.ok(result);
    }

    /**
     * Monthly fraud cases bar chart.
     * GET /api/charts/monthly-cases?months=6
     */
    @GetMapping("/monthly-cases")
    public ResponseEntity<Map<String, Object>> monthlyCases(
            @RequestParam(defaultValue = "6") int months,
            Authentication auth) {

        List<FraudCaseResponse> allCases = fraudCaseService.getCases(null);
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM");
        LinkedHashMap<String, Long> data = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            data.put(now.minusMonths(i).format(monthFmt), 0L);
        }
        allCases.stream()
                .filter(c -> c.createdAt() != null && c.createdAt().isAfter(now.minusMonths(months)))
                .forEach(c -> data.merge(c.createdAt().format(monthFmt), 1L, Long::sum));

        // Vibrant colors for each bar
        List<String> colors = List.of(
            "#6366f1","#f59e0b","#10b981","#ef4444","#3b82f6","#8b5cf6"
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", new ArrayList<>(data.keySet()));
        result.put("values", new ArrayList<>(data.values()));
        result.put("colors", colors.subList(0, Math.min(months, colors.size())));
        return ResponseEntity.ok(result);
    }

    /**
     * Case status distribution for donut chart.
     * GET /api/charts/status-distribution
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<Map<String, Object>> statusDistribution(Authentication auth) {
        List<FraudCaseResponse> allCases = fraudCaseService.getCases(null);

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("OPEN", allCases.stream().filter(c -> c.caseStatus() == CaseStatus.OPEN).count());
        counts.put("CONFIRMED_FRAUD", allCases.stream().filter(c -> c.caseStatus() == CaseStatus.CONFIRMED_FRAUD).count());
        counts.put("FALSE_POSITIVE", allCases.stream().filter(c -> c.caseStatus() == CaseStatus.FALSE_POSITIVE).count());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", List.of("Open", "Confirmed Fraud", "False Positive"));
        result.put("values", new ArrayList<>(counts.values()));
        result.put("colors", List.of("#f59e0b", "#ef4444", "#10b981"));
        return ResponseEntity.ok(result);
    }

    /**
     * Algorithm performance bar chart.
     * GET /api/charts/algorithm-performance
     */
    @GetMapping("/algorithm-performance")
    public ResponseEntity<Map<String, Object>> algorithmPerformance(Authentication auth) {
        List<AlgorithmVersion> versions = algorithmService.listVersions()
                .stream().limit(6).toList();

        List<String> labels = versions.stream()
                .map(AlgorithmVersion::getVersionName).toList();
        List<Double> accuracy = versions.stream()
                .map(v -> v.getAccuracy() != null ? v.getAccuracy() : 0.0).toList();
        List<String> colors = versions.stream()
                .map(v -> Boolean.TRUE.equals(v.getActive()) ? "#6366f1" : "#374151").toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("accuracy", accuracy);
        result.put("colors", colors);
        return ResponseEntity.ok(result);
    }
}
