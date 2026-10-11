package com.guvi.frauddetection.rule;

import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fraud Detection Rule Engine Tests")
class RuleEvaluationTest {

    private DetectionConfig config;
    private HighAmountRule highAmountRule;
    private OddHoursRule oddHoursRule;
    private CompositeFraudEngine compositeEngine;

    @BeforeEach
    void setUp() {
        config = new DetectionConfig();
        config.setFlagThreshold(0.40);
        config.setBlockThreshold(0.70);
        config.setHighAmountLimit(new BigDecimal("50000.00"));
        config.setMaxTxPerHour(5);
        config.setAmountWeight(0.40);
        config.setVelocityWeight(0.30);
        config.setLocationWeight(0.15);
        config.setDeviceWeight(0.15);

        highAmountRule = new HighAmountRule();
        oddHoursRule = new OddHoursRule();
        compositeEngine = new CompositeFraudEngine(List.of(highAmountRule, oddHoursRule));
    }

    @Test
    @DisplayName("HighAmountRule should pass for small transaction below threshold")
    void testSmallAmountPasses() {
        Transaction tx = Transaction.builder()
                .amount(new BigDecimal("1500.00"))
                .transactionTime(LocalDateTime.now())
                .merchant("Amazon")
                .location("Mumbai")
                .deviceId("dev-1")
                .paymentMethod("CARD")
                .build();

        RuleContext ctx = RuleContext.builder()
                .config(config)
                .historicalAverageSpend(2000.0)
                .build();

        RuleEvaluationResult result = highAmountRule.evaluate(tx, ctx);

        assertNotNull(result);
        assertFalse(result.isTriggered());
        assertEquals(0.0, result.getRiskScore());
    }

    @Test
    @DisplayName("HighAmountRule should trigger for transaction exceeding threshold")
    void testHighAmountTriggers() {
        Transaction tx = Transaction.builder()
                .amount(new BigDecimal("85000.00"))
                .transactionTime(LocalDateTime.now())
                .merchant("Jewellery Mart")
                .location("Delhi")
                .deviceId("dev-1")
                .paymentMethod("CARD")
                .build();

        RuleContext ctx = RuleContext.builder()
                .config(config)
                .historicalAverageSpend(5000.0)
                .build();

        RuleEvaluationResult result = highAmountRule.evaluate(tx, ctx);

        assertNotNull(result);
        assertTrue(result.isTriggered());
        assertTrue(result.getRiskScore() > 0.40);
        assertTrue(result.getReason().contains("exceeds"));
    }

    @Test
    @DisplayName("HighAmountRule overloaded risk calculation returns expected ratios")
    void testOverloadedRiskCalculation() {
        double riskA = highAmountRule.calculateAmountRisk(25000.0, 50000.0);
        double riskB = highAmountRule.calculateAmountRisk(100000.0, 50000.0);

        assertTrue(riskA < 1.0);
        assertEquals(1.0, riskB);
    }

    @Test
    @DisplayName("OddHoursRule triggers during late night hours (2 AM)")
    void testOddHoursRule() {
        LocalDateTime lateNight = LocalDateTime.of(2026, 10, 11, 2, 30);
        Transaction tx = Transaction.builder()
                .amount(new BigDecimal("5000.00"))
                .transactionTime(lateNight)
                .merchant("Casino")
                .location("Goa")
                .deviceId("dev-1")
                .paymentMethod("CARD")
                .build();

        RuleContext ctx = RuleContext.builder().config(config).build();
        RuleEvaluationResult result = oddHoursRule.evaluate(tx, ctx);

        assertTrue(result.isTriggered());
        assertTrue(result.getRiskScore() > 0.0);
    }

    @Test
    @DisplayName("CompositeFraudEngine aggregates polymorphic rule evaluation results")
    void testCompositeEngine() {
        Transaction tx = Transaction.builder()
                .amount(new BigDecimal("95000.00"))
                .transactionTime(LocalDateTime.of(2026, 10, 11, 3, 0))
                .merchant("Unknown Vendor")
                .location("Foreign")
                .deviceId("dev-99")
                .paymentMethod("CARD")
                .build();

        RuleContext ctx = RuleContext.builder()
                .config(config)
                .historicalTransactions(Collections.emptyList())
                .knownLocations(Collections.emptySet())
                .knownDevices(Collections.emptySet())
                .historicalAverageSpend(1000.0)
                .build();

        CompositeFraudEngine.CompositeEvaluationResult result = compositeEngine.evaluate(tx, ctx);

        assertNotNull(result);
        assertTrue(result.getOverallScore() > 0.0);
        assertFalse(result.getReasons().isEmpty());
    }
}
