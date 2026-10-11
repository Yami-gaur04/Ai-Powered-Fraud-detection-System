package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.service.FraudCaseService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User alerts controller.
 * Shows user-specific fraud alerts with live polling support.
 *
 * Uses a ConcurrentHashMap as an in-memory per-user "read" tracker
 * to support the unread count without modifying the backend schema.
 */
@Controller
@RequestMapping("/user/alerts")
public class UserAlertsController {

    private final FraudCaseService fraudCaseService;

    public UserAlertsController(FraudCaseService fraudCaseService) {
        this.fraudCaseService = fraudCaseService;
    }

    // Thread-safe in-memory store: email -> last known alert count (for unread detection)
    private static final ConcurrentHashMap<String, Integer> lastSeenCounts = new ConcurrentHashMap<>();

    /**
     * Show user's alert list (redirects to Alerts tab in dashboard).
     * GET /user/alerts
     */
    @GetMapping
    public String alerts(Authentication auth) {
        if (auth != null) {
            String email = auth.getName();
            List<FraudCaseResponse> alerts = fraudCaseService.alertsForUser(email);
            lastSeenCounts.put(email, alerts.size());
        }
        return "redirect:/user/dashboard?tab=alerts";
    }

    /**
     * JSON polling endpoint for live alert count updates.
     * GET /user/alerts/poll
     * Returns: {"total": N, "unread": M, "newAlerts": [...last3...]}
     */
    @GetMapping(value = "/poll", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> pollAlerts(Authentication auth) {
        if (auth == null) {
            return org.springframework.http.ResponseEntity.ok(java.util.Collections.emptyMap());
        }
        String email = auth.getName();
        List<FraudCaseResponse> alerts = fraudCaseService.alertsForUser(email);

        int lastSeen = lastSeenCounts.getOrDefault(email, 0);
        int unread = Math.max(0, alerts.size() - lastSeen);

        List<java.util.Map<String, Object>> newAlerts = alerts.stream()
                .limit(3)
                .map(a -> {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("caseId", a.caseId());
                    m.put("amount", a.amount());
                    m.put("merchant", a.merchant());
                    m.put("fraudScore", a.fraudScore());
                    m.put("status", a.caseStatus().name());
                    m.put("createdAt", a.createdAt() != null ? a.createdAt().toString() : "");
                    return m;
                }).toList();

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("total", alerts.size());
        response.put("unread", unread);
        response.put("newAlerts", newAlerts);
        return org.springframework.http.ResponseEntity.ok(response);
    }
}
