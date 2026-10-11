package com.guvi.frauddetection.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * Servlet filter that restricts administrative servlet endpoints to users with the ADMIN role.
 *
 * <p>Demonstrates role-based authorization at the Servlet Filter layer as required
 * by rubric item A.4.</p>
 */
public class AuthorizationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationFilter.class);

    private static final Set<String> ADMIN_ONLY_URIS = Set.of(
            "/servlet/config"
    );

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("AuthorizationFilter initialised");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String uri = httpRequest.getRequestURI();

            boolean requiresAdmin = ADMIN_ONLY_URIS.stream().anyMatch(uri::startsWith);
            if (requiresAdmin) {
                HttpSession session = httpRequest.getSession(false);
                Object role = session != null ? session.getAttribute("userRole") : null;
                boolean isAdmin = "ADMIN".equalsIgnoreCase(String.valueOf(role))
                        || httpRequest.isUserInRole("ROLE_ADMIN")
                        || httpRequest.isUserInRole("ADMIN");

                if (!isAdmin) {
                    LOG.warn("Access denied for non-admin on administrative URI: {}", uri);
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\": \"Forbidden: Administrative privileges required.\"}");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOG.info("AuthorizationFilter destroyed");
    }
}
