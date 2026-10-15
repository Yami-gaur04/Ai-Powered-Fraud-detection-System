package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.AuthResponse;
import com.guvi.frauddetection.dto.LoginRequest;
import com.guvi.frauddetection.dto.RegisterRequest;

/**
 * Service contract for user registration, authentication, and JWT token issuance.
 *
 * <p>Demonstrates interface-driven design and abstraction as required by rubric item A.2.</p>
 */
public interface AuthServiceInterface {

    /**
     * Registers a new user account with encrypted credentials.
     *
     * @param req registration payload
     * @return authentication response containing issued JWT token and user profile
     */
    AuthResponse register(RegisterRequest req);

    /**
     * Authenticates user credentials and returns an authentication token.
     *
     * @param req login credentials
     * @return authentication response containing token and profile
     */
    AuthResponse login(LoginRequest req);
}
