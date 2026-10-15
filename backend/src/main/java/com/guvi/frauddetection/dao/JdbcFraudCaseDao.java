package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.entity.CaseStatus;
import com.guvi.frauddetection.entity.FraudCase;
import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of the {@link FraudCaseDao} interface.
 * Uses explicit PreparedStatement, ResultSet mapping, try-with-resources,
 * and transaction handling (commit/rollback) for multi-step writes.
 *
 * @see FraudCaseDao
 */
@Repository("jdbcFraudCaseDao")
public class JdbcFraudCaseDao implements FraudCaseDao {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcFraudCaseDao.class);

    private final DataSourceProvider dataSourceProvider;

    /**
     * Constructs the DAO with the centralised data-source provider.
     *
     * @param dataSourceProvider pooled connection provider
     */
    public JdbcFraudCaseDao(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    /** {@inheritDoc} */
    @Override
    public FraudCase save(FraudCase entity) {
        String insertSql = "INSERT INTO fraud_cases (transaction_id, fraud_score, reason, status, admin_feedback, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE fraud_cases SET transaction_id=?, fraud_score=?, reason=?, status=?, "
                + "admin_feedback=?, created_at=? WHERE id=?";

        try (Connection conn = dataSourceProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (entity.getId() == null) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        setCaseParams(ps, entity);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                entity.setId(keys.getLong(1));
                            }
                        }
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        setCaseParams(ps, entity);
                        ps.setLong(7, entity.getId());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                LOG.debug("Saved fraud case id={}", entity.getId());
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(new DataAccessException("Failed to save fraud case", ex));
        }
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<FraudCase> findById(Long id) {
        String sql = "SELECT * FROM fraud_cases WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Error finding fraud case by id={}", id, ex);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<FraudCase> findAll() {
        List<FraudCase> list = new ArrayList<>();
        String sql = "SELECT * FROM fraud_cases ORDER BY created_at DESC";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            LOG.error("Error fetching all fraud cases", ex);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM fraud_cases WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOG.error("Error deleting fraud case id={}", id, ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM fraud_cases WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOG.error("Error checking existence of fraud case id={}", id, ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM fraud_cases";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException ex) {
            LOG.error("Error counting fraud cases", ex);
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<FraudCase> findAllOrderByCreatedAtDesc() throws DataAccessException {
        List<FraudCase> list = new ArrayList<>();
        String sql = "SELECT * FROM fraud_cases ORDER BY created_at DESC";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch all fraud cases", ex);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public List<FraudCase> findByStatus(CaseStatus status) throws DataAccessException {
        List<FraudCase> list = new ArrayList<>();
        String sql = "SELECT * FROM fraud_cases WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch fraud cases by status " + status, ex);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<FraudCase> findByTransactionId(Long transactionId) throws DataAccessException {
        String sql = "SELECT * FROM fraud_cases WHERE transaction_id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find fraud case by transaction " + transactionId, ex);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public long countByStatus(CaseStatus status) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM fraud_cases WHERE status = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count fraud cases by status " + status, ex);
        }
    }

    // ─── Private helpers ───

    /**
     * Sets PreparedStatement parameters from a FraudCase entity.
     *
     * @param ps     the prepared statement
     * @param entity the fraud case entity
     * @throws SQLException if a parameter cannot be set
     */
    private void setCaseParams(PreparedStatement ps, FraudCase entity) throws SQLException {
        ps.setLong(1, entity.getTransaction() != null ? entity.getTransaction().getId() : 0L);
        if (entity.getFraudScore() != null) {
            ps.setDouble(2, entity.getFraudScore());
        } else {
            ps.setNull(2, Types.DOUBLE);
        }
        ps.setString(3, entity.getReason());
        ps.setString(4, entity.getStatus() != null ? entity.getStatus().name() : CaseStatus.OPEN.name());
        ps.setString(5, entity.getAdminFeedback());
        ps.setTimestamp(6, entity.getCreatedAt() != null
                ? Timestamp.valueOf(entity.getCreatedAt()) : null);
    }

    /**
     * Maps a single ResultSet row to a FraudCase entity.
     *
     * @param rs the positioned result set
     * @return a populated FraudCase object
     * @throws SQLException if a column cannot be read
     */
    private FraudCase mapRow(ResultSet rs) throws SQLException {
        FraudCase fc = new FraudCase();
        fc.setId(rs.getLong("id"));
        double score = rs.getDouble("fraud_score");
        fc.setFraudScore(rs.wasNull() ? null : score);
        fc.setReason(rs.getString("reason"));
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            fc.setStatus(CaseStatus.valueOf(statusStr));
        }
        fc.setAdminFeedback(rs.getString("admin_feedback"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            fc.setCreatedAt(ts.toLocalDateTime());
        }
        // Set a minimal Transaction reference
        Transaction tx = new Transaction();
        tx.setId(rs.getLong("transaction_id"));
        fc.setTransaction(tx);
        return fc;
    }
}
