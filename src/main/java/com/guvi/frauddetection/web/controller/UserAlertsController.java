package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.service.FraudCaseService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserAlertsController {

    private final FraudCaseService fraudCaseService;

    // Thread-safe in-memory store: email -> last known alert count (for unread detection)
    private static final ConcurrentHashMap<String, Integer> lastSeenCounts = new ConcurrentHashMap<>();

    /**
     * Show user's alert list.
     * GET /user/alerts
     */
    @GetMapping
    public String alerts(Model model, Authentication auth) {
        String email = auth.getName();
        List<FraudCaseResponse> alerts = fraudCaseService.alertsForUser(email);

        long openCount = alerts.stream().filter(a -> a.caseStatus() == CaseStatus.OPEN).count();
        long confirmedCount = alerts.stream().filter(a -> a.caseStatus() == CaseStatus.CONFIRMED_FRAUD).count();
        long falsePositiveCount = alerts.stream().filter(a -> a.caseStatus() == CaseStatus.FALSE_POSITIVE).count();

        // Mark all as "seen" (update the in-memory read tracker)
        lastSeenCounts.put(email, alerts.size());

        model.addAttribute("alerts", alerts);
        model.addAttribute("openCount", openCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("falsePositiveCount", falsePositiveCount);
        model.addAttribute("pageTitle", "My Alerts");
        model.addAttribute("pageSubtitle", "Fraud alerts for your account");
        model.addAttribute("currentUser", email);
        return "user/alerts";
    }

    /**
     * JSON polling endpoint for live alert count updates.
     * GET /user/alerts/poll
     * Returns: {"total": N, "unread": M, "newAlerts": [...last3...]}
     */
    @GetMapping(value = "/poll", produces = "application/json")
    @ResponseBody
    public java.util.Map<String, Object> pollAlerts(Authentication auth) {
        String email = auth.getName();
        List<FraudCaseResponse> alerts = fraudCaseService.alertsForUser(email);

        int lastSeen = lastSeenCounts.getOrDefault(email, 0);
        int unread = Math.max(0, alerts.size() - lastSeen);

        // Return newest alerts for toast display
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
        return response;
    }
}
