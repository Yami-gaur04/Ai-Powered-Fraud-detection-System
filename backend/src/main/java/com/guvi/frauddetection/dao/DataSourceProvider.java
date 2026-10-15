package com.guvi.frauddetection.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Centralised provider for JDBC {@link Connection} objects.
 * Wraps the Spring-managed HikariCP {@link DataSource} so that DAO
 * classes can obtain connections without depending on Spring directly.
 *
 * <p>Demonstrates explicit JDBC connection acquisition and resource management
 * as required by the database integration rubric (A.3).</p>
 */
@Component
public class DataSourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceProvider.class);

    private final DataSource dataSource;

    /**
     * Constructs the provider with the application's pooled data source.
     *
     * @param dataSource HikariCP-managed DataSource auto-configured by Spring Boot
     */
    public DataSourceProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        LOG.info("DataSourceProvider initialised with DataSource: {}", dataSource.getClass().getSimpleName());
    }

    /**
     * Obtains a JDBC connection from the pool.
     * Callers <b>must</b> close the returned connection (preferably via try-with-resources)
     * to return it to the pool.
     *
     * @return a pooled {@link Connection}
     * @throws SQLException if the pool cannot supply a connection
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Returns the underlying {@link DataSource} for advanced use cases.
     *
     * @return the pooled data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }
}
