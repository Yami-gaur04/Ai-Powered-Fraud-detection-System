package com.guvi.frauddetection.servlet;

import com.guvi.frauddetection.service.TransactionService;
import com.guvi.frauddetection.dto.TransactionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * Jakarta Servlet for transaction operations.
 * Demonstrates explicit servlet usage with service-layer delegation.
 *
 * <p>URL: {@code /servlet/transactions}</p>
 */
public class TransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TransactionServlet.class);

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the servlet with its service dependency.
     *
     * @param transactionService the transaction processing service
     */
    public TransactionServlet(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handles GET requests by returning the authenticated user's transactions as JSON.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if servlet processing fails
     * @throws IOException      if response writing fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("TransactionServlet.doGet invoked");

        Principal principal = req.getUserPrincipal();
        if (principal == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);

        List<TransactionResponse> transactions = transactionService.myTransactions(principal.getName());
        objectMapper.writeValue(resp.getOutputStream(), transactions);
    }
}
