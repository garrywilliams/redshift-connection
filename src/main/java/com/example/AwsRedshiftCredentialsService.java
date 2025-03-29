package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.GetClusterCredentialsRequest;
import software.amazon.awssdk.services.redshift.model.GetClusterCredentialsResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;

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

    private final DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

    public GetClusterCredentialsResponse getCredentials() {
        String dbUser = resolveDbUser();

        RedshiftClient redshift = getRedshiftClient();

        return redshift.getClusterCredentials(GetClusterCredentialsRequest.builder()
                .clusterIdentifier(clusterId)
                .dbUser(dbUser)
                .dbName(dbName)
                .autoCreate(false) // set to true if allowed in your IAM policy
                .build());
    }

    public String getClusterHost() {
        return getCluster().endpoint().address();
    }

    public int getClusterPort() {
        return getCluster().endpoint().port();
    }

    private Cluster getCluster() {
        RedshiftClient redshift = getRedshiftClient();
        return redshift.describeClusters(DescribeClustersRequest.builder()
                        .clusterIdentifier(clusterId)
                        .build())
                .clusters()
                .get(0);
    }

    private RedshiftClient getRedshiftClient() {
        return RedshiftClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private String resolveDbUser() {
        if (configuredDbUser != null && !configuredDbUser.isBlank()) {
            System.out.println("👤 Using configured dbUser: " + configuredDbUser);
            return configuredDbUser;
        }

        // Fallback to IAM caller identity (e.g. for local testing)
        StsClient sts = StsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        var callerArn = sts.getCallerIdentity(GetCallerIdentityRequest.builder().build()).arn();
        var parts = callerArn.split("/");
        var rawName = parts[parts.length - 1];
        var fallbackUser = rawName.replaceAll("[^a-zA-Z0-9_+.@$\\-]", "_");

        System.out.println("👤 Fallback dbUser resolved from ARN: " + fallbackUser);
        return fallbackUser;
    }
}
