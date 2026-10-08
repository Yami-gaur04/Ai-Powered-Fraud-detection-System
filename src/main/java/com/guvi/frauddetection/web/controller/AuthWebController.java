package com.guvi.frauddetection.web.controller;

import com.guvi.frauddetection.dto.RegisterRequest;
import com.guvi.frauddetection.entity.User;
import com.guvi.frauddetection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles /login, /register, /dashboard (role redirect) for the Thymeleaf web UI.
 * Spring Security's form login handles actual authentication at POST /login.
 */
@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Root URL redirect to dashboard / login. */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /** Show login page. */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    /** Show registration page. */
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    /**
     * Process registration form.
     * After successful registration, redirects to /login with a flash success message.
     */
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttrs) {
        // Basic validation
        if (!password.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        if (password.length() < 8) {
            redirectAttrs.addFlashAttribute("error", "Password must be at least 8 characters.");
            return "redirect:/register";
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            redirectAttrs.addFlashAttribute("error", "Email already registered. Please login.");
            return "redirect:/register";
        }

        userRepository.save(User.builder()
                .name(name.trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(password))
                .role(com.guvi.frauddetection.entity.Role.USER)
                .build());

        redirectAttrs.addFlashAttribute("success", "Account created! Please login.");
        return "redirect:/login";
    }

    /** Smart redirect: admins go to /admin/dashboard, users to /user/dashboard. */
    @GetMapping("/dashboard")
    public String dashboard(org.springframework.security.core.Authentication auth) {
        if (auth == null) return "redirect:/login";
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
    }

    /** Access denied page. */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
