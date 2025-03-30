package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
public class RedshiftDataSourceConfig {

    private final AwsRedshiftCredentialsService credentialsService;
    private RefreshableDataSource refreshableDataSource;

    public RedshiftDataSourceConfig(AwsRedshiftCredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Bean
    public DataSource dataSource() {
        var hikari = createNewDataSource();
        this.refreshableDataSource = new RefreshableDataSource(hikari);
        return refreshableDataSource;
    }

    private HikariDataSource createNewDataSource() {
        var creds = credentialsService.getCredentials();

        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:redshift:iam://" + creds.host() + ":" + creds.port() + "/" + creds.dbName());

        System.out.println("🔐 Using JDBC URL: " + config.getJdbcUrl());

        config.setUsername(creds.dbUser());
        config.setPassword(creds.dbPassword());
        config.setMaximumPoolSize(5);
        config.setPoolName("RedshiftPool");

        return new HikariDataSource(config);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void refreshCredentials() {
        var newDataSource = createNewDataSource();
        refreshableDataSource.refresh(newDataSource);
        System.out.println("🔄 Refreshed Redshift credentials + DataSource");
    }
}
