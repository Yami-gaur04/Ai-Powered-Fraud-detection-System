package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.ReviewRequest;
import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.service.FraudCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin reports web controller.
 * Handles fraud case listing, filtering, sorting, pagination, and review actions.
 */
@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportsController {

    private final FraudCaseService fraudCaseService;

    private static final int PAGE_SIZE = 10;

    /**
     * List fraud cases with optional status filter, sorting, and pagination.
     * GET /admin/reports
     */
    @GetMapping
    public String reports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            Model model,
            Authentication auth) {

        CaseStatus filterStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                filterStatus = CaseStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        List<FraudCaseResponse> allCases = fraudCaseService.getCases(filterStatus);

        // Apply search filter (by user email or reason)
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            allCases = allCases.stream()
                    .filter(c -> (c.userEmail() != null && c.userEmail().toLowerCase().contains(q))
                            || (c.reason() != null && c.reason().toLowerCase().contains(q))
                            || (c.merchant() != null && c.merchant().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // Apply sorting using Comparator (demonstrates Collections/Comparator)
        Comparator<FraudCaseResponse> comparator = switch (sort) {
            case "amount"     -> Comparator.comparing(FraudCaseResponse::amount, Comparator.nullsLast(Comparator.naturalOrder()));
            case "fraudScore" -> Comparator.comparing(FraudCaseResponse::fraudScore, Comparator.nullsLast(Comparator.naturalOrder()));
            case "status"     -> Comparator.comparing(c -> c.caseStatus().name());
            default           -> Comparator.comparing(FraudCaseResponse::createdAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("asc".equalsIgnoreCase(dir)) {
            allCases = allCases.stream().sorted(comparator).collect(Collectors.toList());
        } else {
            allCases = allCases.stream().sorted(comparator.reversed()).collect(Collectors.toList());
        }

        // Pagination
        int totalItems = allCases.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (page - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);
        List<FraudCaseResponse> pagedCases = allCases.subList(fromIndex, toIndex);

        // Status distribution for donut chart (LinkedHashMap preserves insertion order)
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (CaseStatus cs : CaseStatus.values()) {
            long count = allCases.stream().filter(c -> c.caseStatus() == cs).count();
            statusCounts.put(cs.name(), count);
        }

        model.addAttribute("cases", pagedCases);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("search", search);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("pageTitle", "Detection Reports");
        model.addAttribute("pageSubtitle", "Fraud case management and analytics");
        model.addAttribute("currentUser", auth.getName());
        return "admin/reports";
    }

    /**
     * Show detail of a single fraud case.
     * GET /admin/reports/detail?id=
     */
    @GetMapping("/detail")
    public String detail(@RequestParam Long id, Model model, Authentication auth) {
        FraudCaseResponse caseDetail = fraudCaseService.getCase(id);
        model.addAttribute("caseDetail", caseDetail);
        model.addAttribute("pageTitle", "Case #" + id + " Detail");
        model.addAttribute("pageSubtitle", "Fraud case review and feedback");
        model.addAttribute("currentUser", auth.getName());
        return "admin/case-detail";
    }

    /**
     * Process admin review (mark as CONFIRMED_FRAUD or FALSE_POSITIVE).
     * POST /admin/reports/review
     */
    @PostMapping("/review")
    public String review(@RequestParam Long caseId,
                         @RequestParam String status,
                         @RequestParam(required = false) String feedback,
                         RedirectAttributes redirectAttrs) {
        try {
            CaseStatus newStatus = CaseStatus.valueOf(status);
            fraudCaseService.review(caseId, new ReviewRequest(newStatus, feedback));
            redirectAttrs.addFlashAttribute("success", "Case #" + caseId + " updated to " + newStatus);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }
}
