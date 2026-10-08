package com.guvi.frauddetection.security;

import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges the existing UserRepository with Spring Security's form login.
 * Returns a UserDetails with ROLE_ADMIN or ROLE_USER authority.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String role = "ROLE_" + user.getRole().name(); // ROLE_ADMIN or ROLE_USER
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }
}
