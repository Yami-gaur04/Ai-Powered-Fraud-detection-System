package com.guvi.frauddetection.controller;

import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** User side: Transaction Monitoring + Transaction Alerts. */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final FraudCaseService fraudCaseService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest req,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.process(auth.getName(), req));
    }

    @GetMapping
    public List<TransactionResponse> myTransactions(Authentication auth) {
        return transactionService.myTransactions(auth.getName());
    }

    @GetMapping("/alerts")
    public List<FraudCaseResponse> myAlerts(Authentication auth) {
        return fraudCaseService.alertsForUser(auth.getName());
    }
}
