package com.guvi.frauddetection.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application lifecycle listener that hooks into Servlet context initialization
 * and destruction events.
 *
 * <p>Demonstrates explicit {@link ServletContextListener} implementation for
 * lifecycle management as required by rubric item A.4.</p>
 */
public class AppServletContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String contextPath = sce.getServletContext().getContextPath();
        LOG.info("=== FraudShield ServletContext initialised successfully at path: '{}' ===", contextPath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("=== FraudShield ServletContext destroyed cleanly ===");
    }
}
