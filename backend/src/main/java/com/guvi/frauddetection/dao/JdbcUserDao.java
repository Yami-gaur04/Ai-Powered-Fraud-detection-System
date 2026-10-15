package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.entity.Role;
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
 * JDBC-based implementation of the {@link UserDao} interface.
 * Uses explicit PreparedStatement, ResultSet mapping, and try-with-resources
 * to demonstrate raw JDBC database connectivity.
 *
 * @see UserDao
 */
@Repository("jdbcUserDao")
public class JdbcUserDao implements UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcUserDao.class);

    private final DataSourceProvider dataSourceProvider;

    /**
     * Constructs the DAO with the centralised data-source provider.
     *
     * @param dataSourceProvider pooled connection provider
     */
    public JdbcUserDao(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    /** {@inheritDoc} */
    @Override
    public User save(User entity) {
        String insertSql = "INSERT INTO users (name, email, password, role, two_factor_enabled, "
                + "daily_spending_limit, alert_threshold, phone, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE users SET name=?, email=?, password=?, role=?, two_factor_enabled=?, "
                + "daily_spending_limit=?, alert_threshold=?, phone=?, created_at=? WHERE id=?";

        try (Connection conn = dataSourceProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (entity.getId() == null) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        setUserParams(ps, entity);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                entity.setId(keys.getLong(1));
                            }
                        }
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        setUserParams(ps, entity);
                        ps.setLong(10, entity.getId());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                LOG.debug("Saved user id={}, email={}", entity.getId(), entity.getEmail());
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(new DataAccessException("Failed to save user", ex));
        }
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Error finding user by id={}", id, ex);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            LOG.error("Error fetching all users", ex);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOG.error("Error deleting user id={}", id, ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOG.error("Error checking existence of user id={}", id, ex);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException ex) {
            LOG.error("Error counting users", ex);
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to find user by email: " + email, ex);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByEmail(String email) throws DataAccessException {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to check email existence: " + email, ex);
        }
    }

    // ─── Private helpers ───

    /**
     * Sets PreparedStatement parameters from a User entity.
     *
     * @param ps     the prepared statement
     * @param entity the user entity
     * @throws SQLException if a parameter cannot be set
     */
    private void setUserParams(PreparedStatement ps, User entity) throws SQLException {
        ps.setString(1, entity.getName());
        ps.setString(2, entity.getEmail());
        ps.setString(3, entity.getPassword());
        ps.setString(4, entity.getRole() != null ? entity.getRole().name() : Role.USER.name());
        ps.setBoolean(5, Boolean.TRUE.equals(entity.getTwoFactorEnabled()));
        ps.setBigDecimal(6, entity.getDailySpendingLimit());
        if (entity.getAlertThreshold() != null) {
            ps.setDouble(7, entity.getAlertThreshold());
        } else {
            ps.setNull(7, Types.DOUBLE);
        }
        ps.setString(8, entity.getPhone());
        ps.setTimestamp(9, entity.getCreatedAt() != null
                ? Timestamp.valueOf(entity.getCreatedAt()) : null);
    }

    /**
     * Maps a single ResultSet row to a User entity.
     *
     * @param rs the positioned result set
     * @return a populated User object
     * @throws SQLException if a column cannot be read
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            user.setRole(Role.valueOf(roleStr));
        }
        user.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
        user.setDailySpendingLimit(rs.getBigDecimal("daily_spending_limit"));
        double threshold = rs.getDouble("alert_threshold");
        user.setAlertThreshold(rs.wasNull() ? null : threshold);
        user.setPhone(rs.getString("phone"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        return user;
    }
}
