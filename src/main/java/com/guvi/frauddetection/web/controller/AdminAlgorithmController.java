package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.AlgorithmUpdateRequest;
import com.guvi.frauddetection.entity.AlgorithmVersion;
import com.guvi.frauddetection.service.AlgorithmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin algorithm management controller.
 * Shows algorithm versions, performance metrics, and allows triggering an update.
 */
@Controller
@RequestMapping("/admin/algorithms")
@RequiredArgsConstructor
public class AdminAlgorithmController {

    private final AlgorithmService algorithmService;

    /**
     * Show list of algorithm versions and performance chart data.
     * GET /admin/algorithms
     */
    @GetMapping
    public String algorithmsPage(Model model, Authentication auth) {
        List<AlgorithmVersion> versions = algorithmService.listVersions();

        model.addAttribute("versions", versions);
        model.addAttribute("pageTitle", "Algorithm Management");
        model.addAttribute("pageSubtitle", "Monitor and refine detection algorithms with feedback");
        model.addAttribute("currentUser", auth.getName());
        return "admin/algorithms";
    }

    /**
     * Trigger an algorithm update based on reviewed fraud case feedback.
     * POST /admin/algorithms/update  (PRG pattern)
     */
    @PostMapping("/update")
    public String updateAlgorithm(
            @RequestParam(required = false) String versionName,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttrs) {
        try {
            AlgorithmUpdateRequest req = new AlgorithmUpdateRequest(
                    versionName != null && !versionName.isBlank() ? versionName : null,
                    description != null && !description.isBlank() ? description : null);
            AlgorithmVersion updated = algorithmService.updateAlgorithm(req);
            redirectAttrs.addFlashAttribute("success",
                    "Algorithm updated: " + updated.getVersionName()
                            + " (accuracy: " + updated.getAccuracy() + "%)");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/algorithms";
    }
}
