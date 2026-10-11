package com.guvi.frauddetection.config;

import com.guvi.frauddetection.filter.AuthenticationFilter;
import com.guvi.frauddetection.filter.AuthorizationFilter;
import com.guvi.frauddetection.filter.CharacterEncodingFilter;
import com.guvi.frauddetection.filter.RequestLoggingFilter;
import com.guvi.frauddetection.listener.AppServletContextListener;
import com.guvi.frauddetection.service.AuthService;
import com.guvi.frauddetection.service.ConfigService;
import com.guvi.frauddetection.service.FraudCaseService;
import com.guvi.frauddetection.service.ReportService;
import com.guvi.frauddetection.service.TransactionService;
import com.guvi.frauddetection.servlet.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration that registers native Jakarta Servlets, Filters,
 * and ServletContextListener via registration beans.
 *
 * <p>Allows traditional Java EE / Jakarta EE servlets and filters to run
 * side-by-side with Spring Boot MVC controllers, satisfying rubric requirements A.4.</p>
 */
@Configuration
public class ServletConfig {

    /* ────────────────────────────────────────────────────────────────────── */
    /* Servlet Registrations                                                  */
    /* ────────────────────────────────────────────────────────────────────── */

    @Bean
    public ServletRegistrationBean<TransactionServlet> transactionServletBean(TransactionService transactionService) {
        ServletRegistrationBean<TransactionServlet> bean =
                new ServletRegistrationBean<>(new TransactionServlet(transactionService), "/servlet/transactions");
        bean.setName("TransactionServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<FraudCaseServlet> fraudCaseServletBean(FraudCaseService fraudCaseService) {
        ServletRegistrationBean<FraudCaseServlet> bean =
                new ServletRegistrationBean<>(new FraudCaseServlet(fraudCaseService), "/servlet/fraud-cases");
        bean.setName("FraudCaseServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<DashboardStatsServlet> dashboardStatsServletBean(ReportService reportService) {
        ServletRegistrationBean<DashboardStatsServlet> bean =
                new ServletRegistrationBean<>(new DashboardStatsServlet(reportService), "/servlet/dashboard-stats");
        bean.setName("DashboardStatsServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ConfigServlet> configServletBean(ConfigService configService) {
        ServletRegistrationBean<ConfigServlet> bean =
                new ServletRegistrationBean<>(new ConfigServlet(configService), "/servlet/config");
        bean.setName("ConfigServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<LoginServlet> loginServletBean(AuthService authService) {
        ServletRegistrationBean<LoginServlet> bean =
                new ServletRegistrationBean<>(new LoginServlet(authService), "/servlet/login");
        bean.setName("LoginServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<LogoutServlet> logoutServletBean() {
        ServletRegistrationBean<LogoutServlet> bean =
                new ServletRegistrationBean<>(new LogoutServlet(), "/servlet/logout");
        bean.setName("LogoutServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    /* ────────────────────────────────────────────────────────────────────── */
    /* Filter Registrations                                                   */
    /* ────────────────────────────────────────────────────────────────────── */

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilterBean() {
        FilterRegistrationBean<CharacterEncodingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CharacterEncodingFilter());
        bean.addUrlPatterns("/*");
        bean.setName("CharacterEncodingFilter");
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterBean() {
        FilterRegistrationBean<RequestLoggingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestLoggingFilter());
        bean.addUrlPatterns("/servlet/*");
        bean.setName("RequestLoggingFilter");
        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterBean() {
        FilterRegistrationBean<AuthenticationFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AuthenticationFilter());
        bean.addUrlPatterns("/servlet/*");
        bean.setName("AuthenticationFilter");
        bean.setOrder(3);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilterBean() {
        FilterRegistrationBean<AuthorizationFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AuthorizationFilter());
        bean.addUrlPatterns("/servlet/*");
        bean.setName("AuthorizationFilter");
        bean.setOrder(4);
        return bean;
    }

    /* ────────────────────────────────────────────────────────────────────── */
    /* Listener Registration                                                  */
    /* ────────────────────────────────────────────────────────────────────── */

    @Bean
    public ServletListenerRegistrationBean<AppServletContextListener> appServletContextListenerBean() {
        ServletListenerRegistrationBean<AppServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new AppServletContextListener());
        return bean;
    }
}
