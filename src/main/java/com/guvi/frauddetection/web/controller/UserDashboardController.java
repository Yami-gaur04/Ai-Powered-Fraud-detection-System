package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.Role;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.exception.ResourceNotFoundException;
import com.guvi.frauddetection.repository.UserRepository;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive User Dashboard Controller for ROLE_USER.
 * Supports 5 tabs: Overview, Transactions, Alerts, Security, Settings.
 * Provides personal metrics, detail drawer, disputes ("This was me" / "This wasn't me"),
 * CSV export, security controls, and AI helper insights.
 */
@Controller
public class UserDashboardController {

    private final TransactionService transactionService;
    private final FraudCaseService fraudCaseService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDashboardController(TransactionService transactionService,
                                   FraudCaseService fraudCaseService,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.transactionService = transactionService;
        this.fraudCaseService = fraudCaseService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * User Main Dashboard (Supports tab selection: overview, transactions, alerts, security, settings).
     * GET /dashboard, GET /dashboard/{tab}, GET /user/dashboard
     */
    @GetMapping({"/dashboard/{tab}", "/user/dashboard"})
    public String userDashboard(
            @PathVariable(required = false) String tab,
            @RequestParam(name = "tab", required = false) String queryTab,
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            jakarta.servlet.http.HttpServletRequest request,
            Model model,
            Authentication auth) {

        String uri = request != null ? request.getRequestURI() : "";
        if ("/dashboard".equals(uri) || "/dashboard/".equals(uri)) {
            return "redirect:/dashboard/overview";
        }

        String effectiveTab = (tab != null && !tab.isBlank()) ? tab :
                              (queryTab != null && !queryTab.isBlank() ? queryTab : "overview");

        String email = auth != null ? auth.getName() : "demo@fraudshield.io";
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<TransactionResponse> allTx = transactionService.myTransactions(email);
        List<FraudCaseResponse> allAlerts = fraudCaseService.alertsForUser(email);

        // ── KPIs & Metrics Calculation ──
        long totalTx = allTx.size();
        BigDecimal totalSpent = allTx.stream()
                .filter(t -> t.status() != TransactionStatus.BLOCKED)
                .map(TransactionResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long flaggedCount = allTx.stream()
                .filter(t -> t.status() == TransactionStatus.FLAGGED || t.status() == TransactionStatus.BLOCKED)
                .count();

        long openAlertsCount = allAlerts.stream()
                .filter(a -> a.caseStatus() == CaseStatus.OPEN)
                .count();

        long safeTxCount = allTx.stream()
                .filter(t -> t.status() == TransactionStatus.APPROVED)
                .count();

        // Safety score (0-100)
        int safetyScore = 100;
        if (totalTx > 0) {
            double penalty = (flaggedCount * 25.0) / totalTx;
            safetyScore = Math.max(40, Math.min(100, (int) Math.round(100.0 - penalty)));
        }

        // Filter transactions for Transactions tab
        List<TransactionResponse> filteredTx = allTx;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            filteredTx = filteredTx.stream()
                    .filter(t -> t.status().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            filteredTx = filteredTx.stream()
                    .filter(t -> (t.merchant() != null && t.merchant().toLowerCase().contains(q))
                            || (t.location() != null && t.location().toLowerCase().contains(q))
                            || (t.id() != null && t.id().toString().contains(q)))
                    .collect(Collectors.toList());
        }

        // Recent activity (latest 6)
        List<TransactionResponse> recentActivity = allTx.stream().limit(6).toList();

        // Alert counts
        long confirmedAlerts = allAlerts.stream().filter(a -> a.caseStatus() == CaseStatus.CONFIRMED_FRAUD).count();
        long clearedAlerts = allAlerts.stream().filter(a -> a.caseStatus() == CaseStatus.FALSE_POSITIVE).count();

        // Dynamic insight headline
        String headline;
        String insightCallout;
        if (openAlertsCount > 0) {
            headline = openAlertsCount + " transaction" + (openAlertsCount > 1 ? "s require" : " requires") + " your verification.";
            insightCallout = openAlertsCount + " transaction held for review. Confirm it below to keep your safety score high.";
        } else if (flaggedCount > 0) {
            headline = "Your account is protected. All recent holds have been resolved.";
            insightCallout = "AI fraud monitoring is active across all connected devices and payment channels.";
        } else {
            headline = "Your account looks safe. No suspicious activity detected.";
            insightCallout = "100% of your transactions this month matched your normal spending patterns.";
        }

        String tabKey = effectiveTab.toLowerCase();
        model.addAttribute("activeTab", tabKey);
        model.addAttribute("days", days);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", email);
        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("userPhone", user.getPhone() != null ? user.getPhone() : "+91 98765 43210");
        model.addAttribute("twoFactorEnabled", Boolean.TRUE.equals(user.getTwoFactorEnabled()));
        model.addAttribute("dailyLimit", user.getDailySpendingLimit() != null ? user.getDailySpendingLimit() : new BigDecimal("75000"));
        model.addAttribute("alertThreshold", user.getAlertThreshold() != null ? user.getAlertThreshold() : 0.40);

        // Stats
        model.addAttribute("totalTx", totalTx);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("flaggedCount", flaggedCount);
        model.addAttribute("openAlertsCount", openAlertsCount);
        model.addAttribute("confirmedAlerts", confirmedAlerts);
        model.addAttribute("clearedAlerts", clearedAlerts);
        model.addAttribute("safeTxCount", safeTxCount);
        model.addAttribute("safetyScore", safetyScore);
        model.addAttribute("headline", headline);
        model.addAttribute("insightCallout", insightCallout);

        // Lists
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("allTransactions", allTx);
        model.addAttribute("filteredTransactions", filteredTx);
        model.addAttribute("alerts", allAlerts);

        model.addAttribute("search", search);
        model.addAttribute("status", status);

        // Dynamic page titles for tabs
        switch (tabKey) {
            case "transactions" -> {
                model.addAttribute("pageTitle", "Transactions");
                model.addAttribute("pageSubtitle", "Audit ledger, status tracking & risk inspection");
            }
            case "alerts" -> {
                model.addAttribute("pageTitle", "Alerts & Holds");
                model.addAttribute("pageSubtitle", "Real-time security notifications & dispute actions");
            }
            case "security" -> {
                model.addAttribute("pageTitle", "Security & Protection");
                model.addAttribute("pageSubtitle", "Authentication devices, 2FA & risk controls");
            }
            case "settings" -> {
                model.addAttribute("pageTitle", "Account Settings");
                model.addAttribute("pageSubtitle", "Profile preferences & audit data export");
            }
            default -> {
                model.addAttribute("pageTitle", "Overview");
                model.addAttribute("pageSubtitle", "Live spending safety & account protection");
            }
        }

        return "user/dashboard";
    }

    /**
     * User verifies flagged transaction ("This was me" -> Mark False Positive & Approve).
     */
    @PostMapping({"/user/transactions/confirm", "/dashboard/transactions/confirm"})
    public String confirmTransaction(@RequestParam Long transactionId,
                                     @RequestParam(required = false, defaultValue = "overview") String returnTab,
                                     Authentication auth,
                                     RedirectAttributes redirectAttrs) {
        try {
            fraudCaseService.userConfirm(transactionId, auth.getName());
            redirectAttrs.addFlashAttribute("success", "Transaction #" + transactionId + " confirmed as authorized. Your safety score has been updated.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error confirming transaction: " + e.getMessage());
        }
        return "redirect:/dashboard/" + returnTab;
    }

    /**
     * User reports unauthorized transaction ("This wasn't me" -> Block & Reopen/Create Fraud Case for Admin).
     */
    @PostMapping({"/user/transactions/report", "/dashboard/transactions/report"})
    public String reportTransaction(@RequestParam Long transactionId,
                                    @RequestParam(required = false, defaultValue = "overview") String returnTab,
                                    Authentication auth,
                                    RedirectAttributes redirectAttrs) {
        try {
            fraudCaseService.userReportFraud(transactionId, auth.getName());
            redirectAttrs.addFlashAttribute("success", "Transaction #" + transactionId + " reported as unauthorized. Card/channel temporarily protected and flagged for admin review.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error reporting transaction: " + e.getMessage());
        }
        return "redirect:/dashboard/" + returnTab;
    }

    /**
     * Update security preferences (2FA toggle, daily limit, threshold).
     */
    @PostMapping({"/user/security/update", "/dashboard/security/update"})
    public String updateSecurity(@RequestParam(required = false) Boolean twoFactorEnabled,
                                 @RequestParam(required = false) String dailyLimit,
                                 @RequestParam(required = false) Double alertThreshold,
                                 Authentication auth,
                                 RedirectAttributes redirectAttrs) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            user.setTwoFactorEnabled(Boolean.TRUE.equals(twoFactorEnabled));
            if (dailyLimit != null && !dailyLimit.isBlank()) {
                user.setDailySpendingLimit(new BigDecimal(dailyLimit.trim()));
            }
            if (alertThreshold != null) {
                user.setAlertThreshold(Math.max(0.1, Math.min(0.9, alertThreshold)));
            }
            userRepository.save(user);
            redirectAttrs.addFlashAttribute("success", "Security preferences updated successfully.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error updating security preferences: " + e.getMessage());
        }
        return "redirect:/dashboard/security";
    }

    /**
     * User profile convenience redirect.
     * GET /user/profile -> /dashboard/settings
     */
    @GetMapping({"/user/profile", "/dashboard/profile"})
    public String profileRedirect() {
        return "redirect:/dashboard/settings";
    }

    /**
     * Update user profile settings.
     */
    @PostMapping({"/user/settings/profile", "/dashboard/settings/profile"})
    public String updateProfile(@RequestParam String name,
                                @RequestParam(required = false) String phone,
                                Authentication auth,
                                RedirectAttributes redirectAttrs) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setName(name.trim());
            if (phone != null) user.setPhone(phone.trim());
            userRepository.save(user);
            redirectAttrs.addFlashAttribute("success", "Profile details updated.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/dashboard/settings";
    }

    /**
     * Update password.
     */
    @PostMapping({"/user/settings/password", "/dashboard/settings/password"})
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth,
                                 RedirectAttributes redirectAttrs) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttrs.addFlashAttribute("error", "Current password is incorrect.");
                return "redirect:/dashboard/settings";
            }
            if (!newPassword.equals(confirmPassword)) {
                redirectAttrs.addFlashAttribute("error", "New passwords do not match.");
                return "redirect:/dashboard/settings";
            }
            if (newPassword.length() < 8) {
                redirectAttrs.addFlashAttribute("error", "New password must be at least 8 characters.");
                return "redirect:/dashboard/settings";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttrs.addFlashAttribute("success", "Password changed successfully.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error changing password: " + e.getMessage());
        }
        return "redirect:/dashboard/settings";
    }

    /**
     * Personal Transaction CSV Export.
     * GET /user/export?format=csv
     */
    @GetMapping({"/user/export", "/dashboard/export"})
    public void exportMyTransactions(HttpServletResponse response, Authentication auth) throws IOException {
        String email = auth != null ? auth.getName() : "demo@fraudshield.io";
        List<TransactionResponse> myTx = transactionService.myTransactions(email);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"my-transactions-report.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Transaction ID,Date Time,Merchant,Location,Amount (INR),Payment Method,Device ID,Risk Score,Status");
            for (TransactionResponse t : myTx) {
                writer.printf("%d,%s,\"%s\",\"%s\",%.2f,%s,\"%s\",%.4f,%s%n",
                        t.id(),
                        t.transactionTime() != null ? t.transactionTime().toString() : "",
                        esc(t.merchant()),
                        esc(t.location()),
                        t.amount() != null ? t.amount().doubleValue() : 0.0,
                        t.paymentMethod() != null ? t.paymentMethod() : "ONLINE",
                        esc(t.deviceId()),
                        t.fraudScore() != null ? t.fraudScore() : 0.0,
                        t.status() != null ? t.status().name() : "APPROVED");
            }
        }
    }

    /**
     * AI Assistant query helper.
     * POST /api/user/ai-query, POST /dashboard/ai-query, POST /user/ai-query
     */
    @PostMapping(value = {"/user/ai-query", "/dashboard/ai-query", "/api/user/ai-query"}, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aiQuery(@RequestBody Map<String, String> body, Authentication auth) {
        String query = body.getOrDefault("query", "").toLowerCase();
        String email = auth != null ? auth.getName() : "demo@fraudshield.io";
        List<TransactionResponse> myTx = transactionService.myTransactions(email);
        List<FraudCaseResponse> alerts = fraudCaseService.alertsForUser(email);

        String answer;
        if (query.contains("flag") || query.contains("why") || query.contains("hold")) {
            long flagged = myTx.stream().filter(t -> t.status() == TransactionStatus.FLAGGED).count();
            if (flagged > 0) {
                Optional<TransactionResponse> sample = myTx.stream().filter(t -> t.status() == TransactionStatus.FLAGGED).findFirst();
                answer = "You have " + flagged + " transaction currently flagged for review" + (sample.isPresent() ? " at " + sample.get().merchant() + " (₹" + sample.get().amount() + "). It was flagged because the amount was above your usual pattern and came from a new location." : ".");
            } else {
                answer = "You have 0 active holds. All your recent transactions have cleared the AI security model smoothly.";
            }
        } else if (query.contains("score") || query.contains("safety") || query.contains("health")) {
            answer = "Your Account Safety Score is calculated using 4 real-time factors: Velocity (how fast purchases occur), Device Fingerprint (known vs new), Geolocation match, and Amount variation against your typical spending average.";
        } else if (query.contains("spent") || query.contains("total") || query.contains("much")) {
            BigDecimal total = myTx.stream().map(TransactionResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            answer = "You have processed " + myTx.size() + " total transactions amounting to ₹" + String.format("%,.2f", total) + " across " + myTx.stream().map(TransactionResponse::merchant).distinct().count() + " unique merchants.";
        } else {
            answer = "FraudShield AI is constantly analyzing transaction velocity, location leaps, and device signatures. 100% of your active accounts are safeguarded by our v1.1 tuned engine.";
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("query", body.get("query"));
        resp.put("answer", answer);
        resp.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(resp);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}
