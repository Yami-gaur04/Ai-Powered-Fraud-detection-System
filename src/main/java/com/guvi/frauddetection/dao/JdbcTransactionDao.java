package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.entity.Transaction;
import com.guvi.frauddetection.entity.TransactionStatus;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of the {@link TransactionDao} interface.
 * Uses explicit {@link PreparedStatement}, {@link ResultSet} mapping,
 * and try-with-resources for deterministic resource cleanup.
 *
 * <p>Demonstrates raw JDBC usage alongside the existing JPA/Hibernate layer
 * to satisfy rubric requirement A.3 (Database Integration – JDBC).</p>
 */
@Repository("jdbcTransactionDao")
public class JdbcTransactionDao implements TransactionDao {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTransactionDao.class);

    private final DataSourceProvider dataSourceProvider;

    /**
     * Constructs the DAO with the centralised data-source provider.
     *
     * @param dataSourceProvider pooled connection provider
     */
    public JdbcTransactionDao(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    // ─── Repository<Transaction, Long> contract ───

    /**
     * Persists a new transaction or updates an existing one.
     * Uses JDBC transaction handling with explicit commit/rollback.
     *
     * @param entity the transaction to save
     * @return the persisted transaction (with generated ID if new)
     */
    @Override
    public Transaction save(Transaction entity) {
        String insertSql = "INSERT INTO transactions (user_id, amount, merchant, location, device_id, "
                + "payment_method, transaction_time, fraud_score, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE transactions SET user_id=?, amount=?, merchant=?, location=?, "
                + "device_id=?, payment_method=?, transaction_time=?, fraud_score=?, status=? WHERE id=?";

        // Explicit JDBC transaction: setAutoCommit(false), commit, rollback
        try (Connection conn = dataSourceProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (entity.getId() == null) {
                    // INSERT with generated keys
                    try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        setTransactionParams(ps, entity);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                entity.setId(keys.getLong(1));
                            }
                        }
                    }
                } else {
                    // UPDATE existing
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        setTransactionParams(ps, entity);
                        ps.setLong(10, entity.getId());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                LOG.debug("Saved transaction id={}", entity.getId());
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(new DataAccessException("Failed to save transaction", ex));
        }
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT t.*, u.name AS user_name, u.email AS user_email "
                + "FROM transactions t JOIN users u ON t.user_id = u.id WHERE t.id = ?";
        // try-with-resources ensures Connection, PreparedStatement, and ResultSet are closed
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Error finding transaction by id={}", id, ex);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, u.name AS user_name, u.email AS user_email "
                + "FROM transactions t JOIN users u ON t.user_id = u.id "
                + "ORDER BY t.transaction_time DESC";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            LOG.error("Error fetching all transactions", ex);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOG.error("Error deleting transaction id={}", id, ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM transactions WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOG.error("Error checking existence of transaction id={}", id, ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException ex) {
            LOG.error("Error counting transactions", ex);
            return 0L;
        }
    }

    // ─── TransactionDao domain methods ───

    /** {@inheritDoc} */
    @Override
    public List<Transaction> findByUserId(Long userId) throws DataAccessException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, u.name AS user_name, u.email AS user_email "
                + "FROM transactions t JOIN users u ON t.user_id = u.id "
                + "WHERE t.user_id = ? ORDER BY t.transaction_time DESC";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to fetch transactions for user " + userId, ex);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public long countByUserId(Long userId) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count transactions for user " + userId, ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long countByStatus(TransactionStatus status) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count transactions by status " + status, ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Double findAverageAmountByUserId(Long userId) throws DataAccessException {
        String sql = "SELECT AVG(amount) FROM transactions WHERE user_id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? null : avg;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to calculate average amount for user " + userId, ex);
        }
        return null;
    }

    // ─── Private helpers ───

    /**
     * Sets PreparedStatement parameters from a Transaction entity.
     *
     * @param ps     the prepared statement
     * @param entity the transaction entity
     * @throws SQLException if a parameter cannot be set
     */
    private void setTransactionParams(PreparedStatement ps, Transaction entity) throws SQLException {
        ps.setLong(1, entity.getUser() != null ? entity.getUser().getId() : 0L);
        ps.setBigDecimal(2, entity.getAmount());
        ps.setString(3, entity.getMerchant());
        ps.setString(4, entity.getLocation());
        ps.setString(5, entity.getDeviceId());
        ps.setString(6, entity.getPaymentMethod());
        ps.setTimestamp(7, entity.getTransactionTime() != null
                ? Timestamp.valueOf(entity.getTransactionTime()) : null);
        if (entity.getFraudScore() != null) {
            ps.setDouble(8, entity.getFraudScore());
        } else {
            ps.setNull(8, Types.DOUBLE);
        }
        ps.setString(9, entity.getStatus() != null ? entity.getStatus().name() : TransactionStatus.APPROVED.name());
    }

    /**
     * Maps a single ResultSet row to a Transaction entity.
     * Demonstrates manual ResultSet-to-object mapping for JDBC rubric.
     *
     * @param rs the positioned result set
     * @return a populated Transaction object
     * @throws SQLException if a column cannot be read
     */
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getLong("id"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setMerchant(rs.getString("merchant"));
        tx.setLocation(rs.getString("location"));
        tx.setDeviceId(rs.getString("device_id"));
        tx.setPaymentMethod(rs.getString("payment_method"));
        Timestamp ts = rs.getTimestamp("transaction_time");
        if (ts != null) {
            tx.setTransactionTime(ts.toLocalDateTime());
        }
        double score = rs.getDouble("fraud_score");
        tx.setFraudScore(rs.wasNull() ? null : score);
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            tx.setStatus(TransactionStatus.valueOf(statusStr));
        }

        // Map the minimal User reference for display purposes
        User user = new User();
        user.setId(rs.getLong("user_id"));
        try {
            user.setName(rs.getString("user_name"));
            user.setEmail(rs.getString("user_email"));
        } catch (SQLException ignored) {
            // Some queries may not join user columns
        }
        tx.setUser(user);
        return tx;
    }
}
