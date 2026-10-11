package com.guvi.frauddetection.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Jakarta Servlet for explicit session termination and user logout.
 * Demonstrates servlet session invalidation and cleanup.
 *
 * <p>URL: {@code /servlet/logout}</p>
 */
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(LogoutServlet.class);

    private final ObjectMapper objectMapper;

    /**
     * Default constructor initializing JSON mapper.
     */
    public LogoutServlet() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handles POST requests by invalidating the current HTTP session.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("LogoutServlet.doPost received logout request from {}", req.getRemoteAddr());

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getOutputStream(), Map.of("message", "Logged out successfully"));
    }
}
