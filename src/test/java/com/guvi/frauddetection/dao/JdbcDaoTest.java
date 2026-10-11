package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.entity.Role;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Database Integration - JDBC DAO Layer Tests")
class JdbcDaoTest {

    @Autowired
    private JdbcUserDao jdbcUserDao;

    @Autowired
    private JdbcTransactionDao jdbcTransactionDao;

    @Test
    @DisplayName("JdbcUserDao persists and retrieves user via raw PreparedStatement and ResultSet")
    void testUserDaoJdbcOperations() throws Exception {
        String email = "jdbctest_" + System.currentTimeMillis() + "@test.com";
        User user = User.builder()
                .name("JDBC Tester")
                .email(email)
                .password("$2a$10$hashedpassword")
                .role(Role.USER)
                .dailySpendingLimit(new BigDecimal("25000.00"))
                .alertThreshold(0.35)
                .twoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = jdbcUserDao.save(user);
        assertNotNull(saved.getId(), "Generated key must be populated via getGeneratedKeys()");

        Optional<User> found = jdbcUserDao.findByEmail(email);
        assertTrue(found.isPresent(), "User should be found by email via JDBC PreparedStatement");
        assertEquals("JDBC Tester", found.get().getName());

        assertTrue(jdbcUserDao.existsByEmail(email));
    }

    @Test
    @DisplayName("JdbcTransactionDao performs explicit JDBC transactions and queries")
    void testTransactionDaoJdbcOperations() throws Exception {
        // Retrieve or create a user for transaction association
        String email = "tx_jdbctest_" + System.currentTimeMillis() + "@test.com";
        User user = jdbcUserDao.save(User.builder()
                .name("Tx Tester")
                .email(email)
                .password("$2a$10$password")
                .role(Role.USER)
                .build());

        Transaction tx = Transaction.builder()
                .user(user)
                .amount(new BigDecimal("1250.75"))
                .merchant("Test Supermarket")
                .location("Hyderabad")
                .deviceId("dev-test-1")
                .paymentMethod("UPI")
                .transactionTime(LocalDateTime.now())
                .fraudScore(0.12)
                .status(TransactionStatus.APPROVED)
                .build();

        Transaction savedTx = jdbcTransactionDao.save(tx);
        assertNotNull(savedTx.getId());

        List<Transaction> userTransactions = jdbcTransactionDao.findByUserId(user.getId());
        assertFalse(userTransactions.isEmpty());
        assertEquals(new BigDecimal("1250.75"), userTransactions.get(0).getAmount());

        long count = jdbcTransactionDao.countByUserId(user.getId());
        assertEquals(1, count);
    }
}
