package com.guvi.frauddetection.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Boolean twoFactorEnabled = false;

    private BigDecimal dailySpendingLimit = new BigDecimal("75000");

    private Double alertThreshold = 0.40;

    private String phone;

    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String name, String email, String password, Role role,
                Boolean twoFactorEnabled, BigDecimal dailySpendingLimit,
                Double alertThreshold, String phone, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.twoFactorEnabled = twoFactorEnabled != null ? twoFactorEnabled : false;
        this.dailySpendingLimit = dailySpendingLimit != null ? dailySpendingLimit : new BigDecimal("75000");
        this.alertThreshold = alertThreshold != null ? alertThreshold : 0.40;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public BigDecimal getDailySpendingLimit() { return dailySpendingLimit; }
    public void setDailySpendingLimit(BigDecimal dailySpendingLimit) { this.dailySpendingLimit = dailySpendingLimit; }

    public Double getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(Double alertThreshold) { this.alertThreshold = alertThreshold; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class UserBuilder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Role role;
        private Boolean twoFactorEnabled = false;
        private BigDecimal dailySpendingLimit = new BigDecimal("75000");
        private Double alertThreshold = 0.40;
        private String phone;
        private LocalDateTime createdAt;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder twoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; return this; }
        public UserBuilder dailySpendingLimit(BigDecimal dailySpendingLimit) { this.dailySpendingLimit = dailySpendingLimit; return this; }
        public UserBuilder alertThreshold(Double alertThreshold) { this.alertThreshold = alertThreshold; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            return new User(id, name, email, password, role, twoFactorEnabled, dailySpendingLimit, alertThreshold, phone, createdAt);
        }
    }
}
