package com.guvi.frauddetection.dao;

import com.guvi.frauddetection.common.Repository;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.exception.DataAccessException;

import java.util.Optional;

/**
 * Data Access Object interface for {@link User} entities.
 * Extends the generic {@link Repository} contract and adds
 * authentication-related lookup methods.
 *
 * @see JdbcUserDao
 */
public interface UserDao extends Repository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for (case-insensitive)
     * @return an {@link Optional} containing the user if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<User> findByEmail(String email) throws DataAccessException;

    /**
     * Checks whether a user exists with the given email.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     * @throws DataAccessException if a database error occurs
     */
    boolean existsByEmail(String email) throws DataAccessException;
}
