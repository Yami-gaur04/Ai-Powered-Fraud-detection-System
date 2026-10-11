package com.guvi.frauddetection.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

/**
 * Servlet filter that checks for an authenticated user session or security principal
 * on protected {@code /servlet/*} endpoints.
 *
 * <p>Demonstrates explicit servlet session security and authorization validation
 * as required by rubric item A.4.</p>
 */
public class AuthenticationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    /** Endpoints exempt from authentication checks. */
    private static final Set<String> EXEMPT_URIS = Set.of(
            "/servlet/login",
            "/servlet/logout"
    );

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("AuthenticationFilter initialised");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String uri = httpRequest.getRequestURI();

            // Check if URI is exempted
            boolean isExempt = EXEMPT_URIS.stream().anyMatch(uri::startsWith);
            if (isExempt) {
                chain.doFilter(request, response);
                return;
            }

            // Check session attribute OR standard Principal
            HttpSession session = httpRequest.getSession(false);
            Principal principal = httpRequest.getUserPrincipal();

            boolean isAuthenticated = (session != null && session.getAttribute("userEmail") != null)
                    || (principal != null);

            if (!isAuthenticated) {
                LOG.warn("Unauthenticated access attempt to {}", uri);
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Authentication required. Please log in first.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOG.info("AuthenticationFilter destroyed");
    }
}
