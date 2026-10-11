package com.guvi.frauddetection.servlet;

import com.guvi.frauddetection.service.ConfigService;
import com.guvi.frauddetection.entity.DetectionConfig;
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
 * Jakarta Servlet for detection configuration operations.
 * Demonstrates explicit HttpServlet usage for admin configuration endpoint.
 *
 * <p>URL: {@code /servlet/config}</p>
 */
public class ConfigServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ConfigServlet.class);

    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the servlet with its service dependency.
     *
     * @param configService the detection configuration service
     */
    public ConfigServlet(ConfigService configService) {
        this.configService = configService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handles GET requests by returning the current detection configuration as JSON.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if servlet processing fails
     * @throws IOException      if response writing fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("ConfigServlet.doGet invoked");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);

        DetectionConfig config = configService.getConfig();
        objectMapper.writeValue(resp.getOutputStream(), config);
    }
}
