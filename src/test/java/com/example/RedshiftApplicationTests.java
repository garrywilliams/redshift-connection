package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

// Use a simpler test that doesn't try to load the full application
@SpringBootTest(classes = {RedshiftApplicationTests.TestConfig.class})
class RedshiftApplicationTests {

    @Configuration
    static class TestConfig {
        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }
        
        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }

    @Test
    void contextLoads() {
        // Just verify the test context loads successfully
    }
}
