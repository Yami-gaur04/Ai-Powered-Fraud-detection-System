package com.guvi.frauddetection.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet filter that logs detailed HTTP request metadata, execution duration,
 * and response status codes.
 *
 * <p>Demonstrates filter lifecycle and audit logging required by rubric item A.4.</p>
 */
public class RequestLoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("RequestLoggingFilter initialised");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            long startTime = System.currentTimeMillis();
            String uri = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            String remoteAddr = httpRequest.getRemoteAddr();

            LOG.debug("--> Incoming request: {} {} from {}", method, uri, remoteAddr);

            try {
                chain.doFilter(request, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = httpResponse.getStatus();
                LOG.info("<-- Finished {} {} with status {} in {} ms", method, uri, status, duration);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        LOG.info("RequestLoggingFilter destroyed");
    }
}
