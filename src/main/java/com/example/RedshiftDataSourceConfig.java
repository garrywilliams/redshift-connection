package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class RedshiftDataSourceConfig {

    private final AwsRedshiftCredentialsService credentialsService;

    public RedshiftDataSourceConfig(AwsRedshiftCredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Bean
    public DataSource dataSource() {
        var creds = credentialsService.getCredentials();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:redshift://redshift-cluster-2.ctvkzfuvyikn.eu-west-2.redshift.amazonaws.com:5439/dev");
        config.setUsername(creds.dbUser());
        config.setPassword(creds.dbPassword());
        config.setDriverClassName("com.amazon.redshift.jdbc42.Driver");

        return new HikariDataSource(config);
    }
}
