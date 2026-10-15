package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.TransactionRequest;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.guvi.frauddetection.entity.Role;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Service Layer - TransactionService Integration Test")
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Process normal transaction approves and persists record")
    void testProcessNormalTransaction() {
        String email = "service_test_" + System.currentTimeMillis() + "@test.com";
        userRepository.save(User.builder()
                .name("Service User")
                .email(email)
                .password("$2a$10$pass")
                .role(Role.USER)
                .build());

        TransactionRequest req = new TransactionRequest(
                new BigDecimal("500.00"),
                "Starbucks",
                "Mumbai",
                "dev-laptop-1",
                "UPI"
        );

        TransactionResponse response = transactionService.process(email, req);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(TransactionStatus.APPROVED, response.status());

        List<TransactionResponse> myTxs = transactionService.myTransactions(email);
        assertFalse(myTxs.isEmpty());
        assertEquals(response.id(), myTxs.get(0).id());
    }

    @Test
    @DisplayName("Process excessive transaction flags or blocks and opens fraud case")
    void testProcessHighRiskTransaction() {
        String email = "risk_test_" + System.currentTimeMillis() + "@test.com";
        userRepository.save(User.builder()
                .name("High Risk Target")
                .email(email)
                .password("$2a$10$pass")
                .role(Role.USER)
                .build());

        // First establish normal baseline history for user (Mumbai, trusted-device-1)
        transactionService.process(email, new TransactionRequest(
                new BigDecimal("1000.00"),
                "Local Grocery",
                "Mumbai",
                "trusted-device-1",
                "UPI"
        ));

        // Subsequent transaction: anomalous amount (150x avg), foreign location, unfamiliar device
        TransactionRequest req = new TransactionRequest(
                new BigDecimal("150000.00"),
                "Crypto Market",
                "Foreign Land",
                "suspicious-device-x",
                "CARD"
        );

        TransactionResponse response = transactionService.process(email, req);

        assertNotNull(response);
        assertNotEquals(TransactionStatus.APPROVED, response.status());
        assertTrue(response.fraudScore() >= 0.40);
        assertTrue(response.message().contains("ALERT") || response.message().contains("Reason"));
    }
}
