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

    @Value("${redshift.host:}")
    private String configuredHost;

    @Value("${redshift.port:#{null}}")
    private Integer configuredPort;

    public RedshiftDbCredentials getCredentials() {
        var credentialsProvider = DefaultCredentialsProvider.create();

        // 🔐 Determine dbUser
        String dbUser;
        if (configuredDbUser != null && !configuredDbUser.isBlank()) {
            dbUser = configuredDbUser;
            System.out.println("👤 Using configured dbUser: " + dbUser);
        } else {
            dbUser = resolveCallerDbUser(credentialsProvider);
            System.out.println("👤 Fallback dbUser resolved from ARN: " + dbUser);
        }

        String host;
        int port;

        if (configuredHost != null && !configuredHost.isBlank()) {
            host = configuredHost;
            port = (configuredPort != null) ? configuredPort : 5439;
            System.out.println("📦 Using configured Redshift host/port: " + host + ":" + port);
        } else {
            System.out.println("🔍 Looking up cluster host/port via DescribeClusters...");
            var redshift = RedshiftClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build();

            var cluster = redshift.describeClusters(DescribeClustersRequest.builder()
                            .clusterIdentifier(clusterId)
                            .build())
                    .clusters()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cluster " + clusterId + " not found"));

            host = cluster.endpoint().address();
            port = cluster.endpoint().port();

            System.out.println("✅ Resolved host: " + host + ", port: " + port);
        }

        // 🪪 Now get credentials using resolved host/port/dbUser
        var redshift = RedshiftClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

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
                port
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
        return rawName.replaceAll("[^a-zA-Z0-9_+.@$\\-]", "_");
    }
}
