package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/user")
public class UserTransactionController {

    private final TransactionService transactionService;
    private final FraudCaseService fraudCaseService;

    public UserTransactionController(TransactionService transactionService, FraudCaseService fraudCaseService) {
        this.transactionService = transactionService;
        this.fraudCaseService = fraudCaseService;
    }

    /**
     * Show all user transactions (redirects to Transactions tab in dashboard).
     * GET /user/transactions
     */
    @GetMapping("/transactions")
    public String transactions() {
        return "redirect:/user/dashboard?tab=transactions";
    }

    /**
     * Show the "Submit New Transaction" form.
     * GET /user/transactions/new
     */
    @GetMapping("/transactions/new")
    public String newTransactionForm(Model model, Authentication auth) {
        model.addAttribute("pageTitle", "New Transaction");
        model.addAttribute("pageSubtitle", "Submit a transaction for fraud analysis");
        model.addAttribute("currentUser", auth.getName());
        return "user/new-transaction";
    }

    /**
     * Process submitted transaction and show result.
     * POST /user/transactions/new
     */
    @PostMapping("/transactions/new")
    public String submitTransaction(
            @RequestParam String amount,
            @RequestParam String merchant,
            @RequestParam String location,
            @RequestParam String deviceId,
            @RequestParam(required = false) String paymentMethod,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttrs) {

        // Validate amount
        BigDecimal amountDecimal;
        try {
            amountDecimal = new BigDecimal(amount.trim());
            if (amountDecimal.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            redirectAttrs.addFlashAttribute("error", "Invalid amount. Must be a positive number.");
            return "redirect:/user/transactions/new";
        }
        if (merchant == null || merchant.isBlank()) {
            redirectAttrs.addFlashAttribute("error", "Merchant/receiver name is required.");
            return "redirect:/user/transactions/new";
        }
        if (location == null || location.isBlank()) {
            redirectAttrs.addFlashAttribute("error", "Location is required.");
            return "redirect:/user/transactions/new";
        }
        if (deviceId == null || deviceId.isBlank()) {
            redirectAttrs.addFlashAttribute("error", "Device ID is required.");
            return "redirect:/user/transactions/new";
        }

        try {
            TransactionRequest req = new TransactionRequest(amountDecimal, merchant.trim(),
                    location.trim(), deviceId.trim(),
                    paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "ONLINE");
            TransactionResponse result = transactionService.process(auth.getName(), req);

            // Show result on the same page (forward to result view via model)
            model.addAttribute("result", result);
            model.addAttribute("pageTitle", "Transaction Result");
            model.addAttribute("pageSubtitle", "Fraud analysis complete");
            model.addAttribute("currentUser", auth.getName());
            return "user/transaction-result";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error processing transaction: " + e.getMessage());
            return "redirect:/user/transactions/new";
        }
    }
}
