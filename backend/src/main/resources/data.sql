-- ============================================================================
-- FraudShield Initial Seed Data DML
-- Default credentials:
--   Admin: admin@fraud.com / Admin@123
--   User:  demo@fraudshield.io / Demo@123
-- ============================================================================

MERGE INTO detection_config (id, flag_threshold, block_threshold, high_amount_limit, max_tx_per_hour, amount_weight, velocity_weight, location_weight, device_weight)
KEY(id)
VALUES (1, 0.40, 0.70, 50000.00, 5, 0.40, 0.30, 0.15, 0.15);
