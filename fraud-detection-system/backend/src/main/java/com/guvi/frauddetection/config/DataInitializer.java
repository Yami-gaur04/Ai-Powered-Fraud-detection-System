package com.guvi.frauddetection.config;

import com.guvi.frauddetection.entity.*;
import com.guvi.frauddetection.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Seeds a default admin, default detection settings and the first algorithm version. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DetectionConfigRepository configRepository;
    private final AlgorithmVersionRepository algorithmRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DetectionConfigRepository configRepository,
                           AlgorithmVersionRepository algorithmRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.configRepository = configRepository;
        this.algorithmRepository = algorithmRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@fraud.com")) {
            userRepository.save(User.builder()
                    .name("System Admin")
                    .email("admin@fraud.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .build());
            System.out.println(">>> Default admin created: admin@fraud.com / Admin@123");
        }

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

        if (algorithmRepository.count() == 0) {
            algorithmRepository.save(AlgorithmVersion.builder()
                    .versionName("v1.0-rule-based")
                    .description("Initial weighted rule-based anomaly scoring (amount, velocity, location, device)")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
