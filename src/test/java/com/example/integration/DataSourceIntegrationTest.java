package com.example.integration;

import com.example.RefreshableDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DataSourceIntegrationTest {

    private HikariDataSource dataSource;
    private RefreshableDataSource refreshableDataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        
        dataSource = new HikariDataSource(config);
        refreshableDataSource = new RefreshableDataSource(dataSource);
        jdbcTemplate = new JdbcTemplate(refreshableDataSource);
        
        // Create test table
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255))");
    }
    
    @AfterEach
    void cleanup() {
        // Clean up data
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS test_table");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        
        // Close the datasource
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    void shouldExecuteQueriesAgainstDatabase() {
        // Given - Make sure table is empty
        jdbcTemplate.execute("DELETE FROM test_table");
        
        // Insert test data
        jdbcTemplate.update("INSERT INTO test_table (id, name) VALUES (?, ?)", 1, "Test Name");
        
        // When
        Integer id = jdbcTemplate.queryForObject(
                "SELECT id FROM test_table WHERE name = ?", 
                Integer.class, 
                "Test Name");
        
        // Then
        assertEquals(1, id);
    }

    @Test
    void shouldRefreshDataSourceAndMaintainConnectivity() throws SQLException {
        // Given - Make sure table is empty
        jdbcTemplate.execute("DELETE FROM test_table");
        
        // Insert test data
        jdbcTemplate.update("INSERT INTO test_table (id, name) VALUES (?, ?)", 1, "Test Name");
        
        // When we refresh the data source
        HikariConfig newConfig = new HikariConfig();
        newConfig.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        newConfig.setUsername("sa");
        newConfig.setPassword("");
        newConfig.setDriverClassName("org.h2.Driver");
        
        HikariDataSource newDataSource = new HikariDataSource(newConfig);
        refreshableDataSource.refresh(newDataSource);
        
        // Then we should still be able to query the database
        Integer id = jdbcTemplate.queryForObject(
                "SELECT id FROM test_table WHERE name = ?", 
                Integer.class, 
                "Test Name");
        assertEquals(1, id);
        
        // And the original data source should be closed
        assertTrue(dataSource.isClosed());
        
        // Set the new data source as current for cleanup
        dataSource = newDataSource;
    }
}
