-- ============================================================================
-- FraudShield Database Schema DDL
-- Compatible with MySQL 8.x and H2 (MySQL Mode)
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    daily_spending_limit DECIMAL(15, 2) DEFAULT 50000.00,
    alert_threshold DOUBLE DEFAULT 0.40,
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS detection_config (
    id BIGINT PRIMARY KEY,
    flag_threshold DOUBLE NOT NULL,
    block_threshold DOUBLE NOT NULL,
    high_amount_limit DECIMAL(15, 2) NOT NULL,
    max_tx_per_hour INT NOT NULL,
    amount_weight DOUBLE NOT NULL,
    velocity_weight DOUBLE NOT NULL,
    location_weight DOUBLE NOT NULL,
    device_weight DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS algorithm_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_name VARCHAR(255) NOT NULL,
    description TEXT,
    accuracy DOUBLE,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_time TIMESTAMP NOT NULL,
    fraud_score DOUBLE,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fraud_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    fraud_score DOUBLE NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    admin_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);

-- Indexes for high-frequency queries
CREATE INDEX IF NOT EXISTS idx_tx_user_time ON transactions(user_id, transaction_time DESC);
CREATE INDEX IF NOT EXISTS idx_tx_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_case_status ON fraud_cases(status);
CREATE INDEX IF NOT EXISTS idx_case_tx ON fraud_cases(transaction_id);
