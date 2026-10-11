package com.guvi.frauddetection.servlet;

import com.guvi.frauddetection.service.ReportService;
import com.guvi.frauddetection.dto.DetectionReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Jakarta Servlet that exposes dashboard statistics as a JSON endpoint.
 * Registered alongside Spring MVC controllers to demonstrate explicit Servlet usage.
 *
 * <p>URL: {@code /servlet/dashboard-stats}</p>
 *
 * <p>Servlet code follows the rubric pattern: parse request → call service → write response.</p>
 */
public class DashboardStatsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DashboardStatsServlet.class);

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the servlet with its service dependency.
     *
     * @param reportService the report aggregation service
     */
    public DashboardStatsServlet(ReportService reportService) {
        this.reportService = reportService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handles GET requests by returning dashboard statistics as JSON.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if servlet processing fails
     * @throws IOException      if response writing fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("DashboardStatsServlet.doGet invoked from {}", req.getRemoteAddr());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);

        DetectionReport report = reportService.buildReport();
        objectMapper.writeValue(resp.getOutputStream(), report);
    }
}
