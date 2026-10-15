package com.guvi.frauddetection.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.frauddetection.dto.AuthResponse;
import com.guvi.frauddetection.dto.LoginRequest;
import com.guvi.frauddetection.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Jakarta Servlet for processing user authentication requests.
 * Demonstrates explicit HttpServlet handling with JSON body parsing,
 * service delegation, session management, and JSON response rendering.
 *
 * <p>URL: {@code /servlet/login}</p>
 */
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(LoginServlet.class);

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the servlet with required dependencies.
     *
     * @param authService the authentication service
     */
    public LoginServlet(AuthService authService) {
        this.authService = authService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handles POST requests by parsing credentials and authenticating the user.
     *
     * @param req  the HTTP request containing JSON login credentials
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("LoginServlet.doPost received authentication attempt from {}", req.getRemoteAddr());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            LoginRequest loginRequest = objectMapper.readValue(req.getInputStream(), LoginRequest.class);
            AuthResponse authResponse = authService.login(loginRequest);

            // Establish servlet HTTP session
            HttpSession session = req.getSession(true);
            session.setAttribute("userEmail", authResponse.email());
            session.setAttribute("userRole", authResponse.role());

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), authResponse);
        } catch (Exception ex) {
            LOG.warn("Authentication failed in LoginServlet: {}", ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), Map.of("error", "Invalid email or password"));
        }
    }
}
