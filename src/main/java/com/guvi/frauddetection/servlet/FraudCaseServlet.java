package com.guvi.frauddetection.servlet;

import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.dto.FraudCaseResponse;
import com.guvi.frauddetection.entity.CaseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Jakarta Servlet for fraud case management operations.
 * Demonstrates explicit HttpServlet usage with doGet and doPost.
 *
 * <p>URL: {@code /servlet/fraud-cases}</p>
 */
public class FraudCaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(FraudCaseServlet.class);

    private final FraudCaseService fraudCaseService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the servlet with its service dependency.
     *
     * @param fraudCaseService the fraud case management service
     */
    public FraudCaseServlet(FraudCaseService fraudCaseService) {
        this.fraudCaseService = fraudCaseService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handles GET requests by returning fraud cases filtered by optional status parameter.
     *
     * @param req  the HTTP request (optional query param: {@code status})
     * @param resp the HTTP response
     * @throws ServletException if servlet processing fails
     * @throws IOException      if response writing fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("FraudCaseServlet.doGet invoked");

        String statusParam = req.getParameter("status");
        CaseStatus status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try {
                status = CaseStatus.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ex) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status: " + statusParam);
                return;
            }
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);

        List<FraudCaseResponse> cases = fraudCaseService.getCases(status);
        objectMapper.writeValue(resp.getOutputStream(), cases);
    }
}
