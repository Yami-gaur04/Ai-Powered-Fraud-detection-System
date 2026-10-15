package com.guvi.frauddetection.config;

import com.guvi.frauddetection.entity.*;
import com.guvi.frauddetection.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Initializes default application data upon startup.
 * Seeds default administrator, demo user, detection configuration,
 * algorithm versions, and demo transactions/fraud cases if not present.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DetectionConfigRepository configRepository;
    private final AlgorithmVersionRepository algorithmRepository;
    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DetectionConfigRepository configRepository,
                           AlgorithmVersionRepository algorithmRepository,
                           TransactionRepository transactionRepository,
                           FraudCaseRepository fraudCaseRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.configRepository = configRepository;
        this.algorithmRepository = algorithmRepository;
        this.transactionRepository = transactionRepository;
        this.fraudCaseRepository = fraudCaseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Default Admin
        if (!userRepository.existsByEmail("admin@fraud.com")) {
            userRepository.save(User.builder()
                    .name("System Admin")
                    .email("admin@fraud.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .twoFactorEnabled(true)
                    .dailySpendingLimit(new BigDecimal("500000"))
                    .alertThreshold(0.35)
                    .createdAt(LocalDateTime.now().minusMonths(6))
                    .build());
            LOG.info(">>> Default admin created: admin@fraud.com / Admin@123");
        }

        // 2. Default Demo User
        User demoUser = userRepository.findByEmail("demo@fraudshield.io").orElse(null);
        if (demoUser == null) {
            demoUser = userRepository.save(User.builder()
                    .name("Alex Vance")
                    .email("demo@fraudshield.io")
                    .password(passwordEncoder.encode("Demo@123"))
                    .role(Role.USER)
                    .twoFactorEnabled(true)
                    .dailySpendingLimit(new BigDecimal("75000"))
                    .alertThreshold(0.40)
                    .phone("+91 98765 43210")
                    .createdAt(LocalDateTime.now().minusMonths(3))
                    .build());
            LOG.info(">>> Demo user created: demo@fraudshield.io / Demo@123");
        }

        // 3. Detection Config
        if (configRepository.count() == 0) {
            DetectionConfig c = new DetectionConfig();
            c.setId(1L);
            c.setFlagThreshold(0.40);
            c.setBlockThreshold(0.70);
            c.setHighAmountLimit(new BigDecimal("50000"));
            c.setMaxTxPerHour(5);
            c.setAmountWeight(0.40);
            c.setVelocityWeight(0.30);
            c.setLocationWeight(0.15);
            c.setDeviceWeight(0.15);
            configRepository.save(c);
        }

        // 4. Algorithm Versions
        if (algorithmRepository.count() == 0) {
            algorithmRepository.save(AlgorithmVersion.builder()
                    .versionName("v1.0-rule-based")
                    .description("Initial weighted rule-based anomaly scoring (amount, velocity, location, device)")
                    .accuracy(92.4)
                    .active(false)
                    .createdAt(LocalDateTime.now().minusMonths(2))
                    .build());

            algorithmRepository.save(AlgorithmVersion.builder()
                    .versionName("v1.1-feedback-tuned")
                    .description("Optimized anomaly weights with false-positive penalty reduction and burst detection")
                    .accuracy(96.8)
                    .active(true)
                    .createdAt(LocalDateTime.now().minusWeeks(2))
                    .build());
        }

        // 5. Seed realistic transactions for demo user if none exist
        if (transactionRepository.countByUserId(demoUser.getId()) == 0) {
            LocalDateTime now = LocalDateTime.now();

            createTx(demoUser, "Apple Store", new BigDecimal("14900.00"), "Mumbai", "dev-macbook-pro", "ONLINE", now.minusDays(27).minusHours(3), 0.08, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Uber Trips", new BigDecimal("450.00"), "Mumbai", "dev-iphone-15", "UPI", now.minusDays(24).minusHours(1), 0.02, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Swiggy Food", new BigDecimal("1240.00"), "Mumbai", "dev-iphone-15", "UPI", now.minusDays(21).minusHours(6), 0.03, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Zara Retail", new BigDecimal("6800.00"), "Mumbai", "dev-iphone-15", "CARD", now.minusDays(19).minusHours(4), 0.12, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Netflix Premium", new BigDecimal("649.00"), "Mumbai", "dev-macbook-pro", "ONLINE", now.minusDays(16).minusHours(12), 0.01, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Amazon India", new BigDecimal("3499.00"), "Mumbai", "dev-macbook-pro", "ONLINE", now.minusDays(14).minusHours(2), 0.05, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Starbucks Coffee", new BigDecimal("820.00"), "Mumbai", "dev-iphone-15", "UPI", now.minusDays(11).minusHours(5), 0.04, TransactionStatus.APPROVED, null, null, null);

            // Flagged transaction (Open case)
            createTx(demoUser, "Unknown Electronics Hub", new BigDecimal("48500.00"), "Bengaluru", "dev-unknown-android", "CARD", now.minusDays(8).minusHours(4), 0.62, TransactionStatus.FLAGGED,
                    CaseStatus.OPEN, "Amount near high threshold (₹48,500) initiated from an unfamiliar device ID and new city", null);

            createTx(demoUser, "Flipkart Wholesale", new BigDecimal("18500.00"), "Mumbai", "dev-macbook-pro", "ONLINE", now.minusDays(6).minusHours(7), 0.22, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Blinkit Quick Commerce", new BigDecimal("1120.00"), "Mumbai", "dev-iphone-15", "UPI", now.minusDays(4).minusHours(2), 0.02, TransactionStatus.APPROVED, null, null, null);

            // Blocked transaction (Confirmed Fraud)
            createTx(demoUser, "DarkWeb Crypto Marketplace", new BigDecimal("125000.00"), "Dubai", "dev-linux-proxy-44", "CARD", now.minusDays(3).minusHours(9), 0.88, TransactionStatus.BLOCKED,
                    CaseStatus.CONFIRMED_FRAUD, "High amount (2.5x threshold) combined with foreign geolocation and suspicious proxy signature", "Confirmed fraudulent card-testing attempt. Transaction permanently blocked.");

            createTx(demoUser, "Steam Digital Games", new BigDecimal("2400.00"), "Mumbai", "dev-macbook-pro", "ONLINE", now.minusDays(2).minusHours(1), 0.10, TransactionStatus.APPROVED, null, null, null);

            // False positive (Cleared)
            createTx(demoUser, "Airbnb Stay Bangalore", new BigDecimal("12000.00"), "Bengaluru", "dev-macbook-pro", "ONLINE", now.minusDays(1).minusHours(8), 0.38, TransactionStatus.APPROVED,
                    CaseStatus.FALSE_POSITIVE, "New location detected during weekend booking", "Verified legitimate by cardholder: 'This was me'");

            // Today's fresh transactions
            createTx(demoUser, "Swiggy Instamart", new BigDecimal("860.00"), "Mumbai", "dev-iphone-15", "UPI", now.minusHours(4), 0.02, TransactionStatus.APPROVED, null, null, null);
            createTx(demoUser, "Amazon Prime Video", new BigDecimal("1499.00"), "Mumbai", "dev-macbook-pro", "ONLINE", now.minusHours(1), 0.04, TransactionStatus.APPROVED, null, null, null);

            LOG.info(">>> Seeded demo transactions and fraud cases for: demo@fraudshield.io");
        }
    }

    private void createTx(User user, String merchant, BigDecimal amount, String location, String deviceId,
                          String paymentMethod, LocalDateTime time, Double score, TransactionStatus status,
                          CaseStatus caseStatus, String caseReason, String adminFeedback) {
        Transaction tx = transactionRepository.save(Transaction.builder()
                .user(user)
                .merchant(merchant)
                .amount(amount)
                .location(location)
                .deviceId(deviceId)
                .paymentMethod(paymentMethod)
                .transactionTime(time)
                .fraudScore(score)
                .status(status)
                .build());

        if (caseStatus != null) {
            fraudCaseRepository.save(FraudCase.builder()
                    .transaction(tx)
                    .fraudScore(score)
                    .reason(caseReason)
                    .status(caseStatus)
                    .adminFeedback(adminFeedback)
                    .createdAt(time)
                    .build());
        }
    }
}
