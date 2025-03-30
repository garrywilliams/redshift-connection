package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;

@Service
public class AwsRedshiftCredentialsService {

    @Value("${redshift.cluster-id}")
    private String clusterId;

    @Value("${redshift.db-name}")
    private String dbName;

    @Value("${redshift.region}")
    private String region;

    @Value("${redshift.db-user:}")
    private String configuredDbUser;

    @Value("${redshift.endpoint:}")
    private String configuredEndpoint;

    @Value("${redshift.port:}")
    private String configuredPort;

    public RedshiftDbCredentials getCredentials() {
        var credentialsProvider = DefaultCredentialsProvider.create();

        String dbUser;
        if (configuredDbUser != null && !configuredDbUser.isBlank()) {
            dbUser = configuredDbUser;
            System.out.println("👤 Using configured dbUser: " + dbUser);
        } else {
            dbUser = resolveCallerDbUser(credentialsProvider);
        }

        var redshift = RedshiftClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        // 🧠 Resolve host and port from config if available, otherwise from DescribeClusters
        String host = configuredEndpoint;
        String port = configuredPort;

        if (host == null || host.isBlank() || port == null || port.isBlank()) {
            System.out.println("🔍 Looking up cluster host/port via DescribeClusters...");
            var cluster = redshift.describeClusters(DescribeClustersRequest.builder()
                            .clusterIdentifier(clusterId)
                            .build())
                    .clusters()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cluster " + clusterId + " not found"));

            host = cluster.endpoint().address();
            port = String.valueOf(cluster.endpoint().port());

            System.setProperty("resolved.redshift.endpoint", host);
            System.setProperty("resolved.redshift.port", port);

            System.out.println("✅ Resolved host: " + host + ", port: " + port);
        } else {
            System.out.println("📦 Using configured Redshift host/port: " + host + ":" + port);
            System.setProperty("resolved.redshift.endpoint", host);
            System.setProperty("resolved.redshift.port", port);
        }

        var credsResponse = redshift.getClusterCredentials(GetClusterCredentialsRequest.builder()
                .clusterIdentifier(clusterId)
                .dbUser(dbUser)
                .dbName(dbName)
                .autoCreate(false)
                .build());

        return new RedshiftDbCredentials(
                dbName,
                dbUser,
                credsResponse.dbPassword(),
                host,
                Integer.parseInt(port)
        );
    }

    private String resolveCallerDbUser(DefaultCredentialsProvider provider) {
        var sts = software.amazon.awssdk.services.sts.StsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(provider)
                .build();

        var callerArn = sts.getCallerIdentity(builder -> {}).arn();
        var parts = callerArn.split("/");
        var rawName = parts[parts.length - 1];
        var dbUser = rawName.replaceAll("[^a-zA-Z0-9_+.@$\\-]", "_");

        System.out.println("👤 Fallback dbUser resolved from ARN: " + dbUser);
        return dbUser;
    }
}
