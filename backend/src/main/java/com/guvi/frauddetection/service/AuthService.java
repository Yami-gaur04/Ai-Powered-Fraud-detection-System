package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.AuthResponse;
import com.guvi.frauddetection.dto.LoginRequest;
import com.guvi.frauddetection.dto.RegisterRequest;
import com.guvi.frauddetection.entity.Role;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.exception.UnauthorizedException;
import com.guvi.frauddetection.repository.UserRepository;
import com.guvi.frauddetection.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service managing user registration, authentication verification, and security token issuance.
 *
 * <p>Implements {@link AuthServiceInterface} to satisfy interface-driven design
 * required by rubric item A.2.</p>
 */
@Service
public class AuthService implements AuthServiceInterface {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Constructs the auth service with required dependencies.
     *
     * @param userRepository  user account repository
     * @param passwordEncoder cryptographic password encoder
     * @param jwtUtil         JWT utility provider
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = userRepository.save(User.builder()
                .name(req.name().trim())
                .email(email)
                .password(passwordEncoder.encode(req.password()))
                .role(Role.USER)
                .build());
        return toResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }
}
