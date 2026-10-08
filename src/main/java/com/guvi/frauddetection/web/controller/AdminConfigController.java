package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.dto.ConfigRequest;
import com.guvi.frauddetection.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Admin configuration settings controller.
 * Shows current DetectionConfig values and processes form updates.
 */
@Controller
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final ConfigService configService;

    /**
     * Display current detection configuration.
     * GET /admin/config
     */
    @GetMapping
    public String configPage(Model model, Authentication auth) {
        DetectionConfig config = configService.getConfig();
        model.addAttribute("config", config);
        model.addAttribute("pageTitle", "Configuration Settings");
        model.addAttribute("pageSubtitle", "Tune detection thresholds and algorithm weights");
        model.addAttribute("currentUser", auth.getName());
        return "admin/config";
    }

    /**
     * Process configuration update form.
     * POST /admin/config  (POST-Redirect-GET pattern with flash message)
     */
    @PostMapping
    public String updateConfig(
            @RequestParam Double flagThreshold,
            @RequestParam Double blockThreshold,
            @RequestParam BigDecimal highAmountLimit,
            @RequestParam Integer maxTxPerHour,
            @RequestParam Double amountWeight,
            @RequestParam Double velocityWeight,
            @RequestParam Double locationWeight,
            @RequestParam Double deviceWeight,
            RedirectAttributes redirectAttrs) {

        // Server-side validation (range checks)
        if (flagThreshold == null || flagThreshold < 0 || flagThreshold > 1) {
            redirectAttrs.addFlashAttribute("error", "Flag threshold must be between 0 and 1.");
            return "redirect:/admin/config";
        }
        if (blockThreshold == null || blockThreshold < 0 || blockThreshold > 1) {
            redirectAttrs.addFlashAttribute("error", "Block threshold must be between 0 and 1.");
            return "redirect:/admin/config";
        }
        if (flagThreshold >= blockThreshold) {
            redirectAttrs.addFlashAttribute("error", "Flag threshold must be lower than block threshold.");
            return "redirect:/admin/config";
        }
        if (highAmountLimit == null || highAmountLimit.compareTo(BigDecimal.ONE) < 0) {
            redirectAttrs.addFlashAttribute("error", "High amount limit must be at least 1.");
            return "redirect:/admin/config";
        }
        if (maxTxPerHour == null || maxTxPerHour < 1) {
            redirectAttrs.addFlashAttribute("error", "Max transactions per hour must be at least 1.");
            return "redirect:/admin/config";
        }

        try {
            ConfigRequest req = new ConfigRequest(flagThreshold, blockThreshold, highAmountLimit,
                    maxTxPerHour, amountWeight, velocityWeight, locationWeight, deviceWeight);
            configService.updateConfig(req);
            redirectAttrs.addFlashAttribute("success", "Configuration updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/config";
    }
}
