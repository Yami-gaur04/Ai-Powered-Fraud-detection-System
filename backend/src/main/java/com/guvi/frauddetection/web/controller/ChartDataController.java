package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.entity.AlgorithmVersion;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.service.AlgorithmService;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping(value = "/api/charts", produces = "application/json")
public class ChartDataController {

    private final FraudCaseService fraudCaseService;
    private final AlgorithmService algorithmService;
    private final TransactionService transactionService;

    public ChartDataController(FraudCaseService fraudCaseService,
                               AlgorithmService algorithmService,
                               TransactionService transactionService) {
        this.fraudCaseService = fraudCaseService;
        this.algorithmService = algorithmService;
        this.transactionService = transactionService;
    }

    /**
     * User personal trend over last N days (area chart: Spending vs Flagged/Rescued).
     * GET /api/charts/user-trend?days=30
     */
    @GetMapping("/user-trend")
    public ResponseEntity<Map<String, Object>> userTrend(
            @RequestParam(defaultValue = "30") int days,
            Authentication auth) {

        String email = auth != null ? auth.getName() : "demo@fraudshield.io";
        List<TransactionResponse> myTx = transactionService.myTransactions(email);

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MMM dd");
        LinkedHashMap<String, BigDecimal> spendingMap = new LinkedHashMap<>();
        LinkedHashMap<String, BigDecimal> flaggedMap = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.format(dayFmt);
            spendingMap.put(key, BigDecimal.ZERO);
            flaggedMap.put(key, BigDecimal.ZERO);
        }

        for (TransactionResponse t : myTx) {
            if (t.transactionTime() != null) {
                LocalDate d = t.transactionTime().toLocalDate();
                if (!d.isBefore(today.minusDays(days))) {
                    String key = d.format(dayFmt);
                    BigDecimal currentSpend = spendingMap.getOrDefault(key, BigDecimal.ZERO);
                    spendingMap.put(key, currentSpend.add(t.amount() != null ? t.amount() : BigDecimal.ZERO));

                    if (t.status() == TransactionStatus.FLAGGED || t.status() == TransactionStatus.BLOCKED) {
                        BigDecimal currentFlag = flaggedMap.getOrDefault(key, BigDecimal.ZERO);
                        flaggedMap.put(key, currentFlag.add(t.amount() != null ? t.amount() : BigDecimal.ZERO));
                    }
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", new ArrayList<>(spendingMap.keySet()));
        result.put("spending", spendingMap.values().stream().map(BigDecimal::doubleValue).toList());
        result.put("flagged", flaggedMap.values().stream().map(BigDecimal::doubleValue).toList());
        return ResponseEntity.ok(result);
    }

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

        // Brand palette colors for bars
        List<String> colors = List.of(
            "#C0302B", "#2F6FB5", "#D98A00", "#1E7B3A", "#8A8A90", "#17181C"
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
        // Open = amber, Confirmed Fraud = red, False Positive = blue
        result.put("colors", List.of("#D98A00", "#C0302B", "#2F6FB5"));
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
                .map(v -> Boolean.TRUE.equals(v.getActive()) ? "#C0302B" : "#8A8A90").toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("accuracy", accuracy);
        result.put("colors", colors);
        return ResponseEntity.ok(result);
    }
}
