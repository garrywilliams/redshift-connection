package com.example;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.GetClusterCredentialsRequest;
import software.amazon.awssdk.services.redshift.model.GetClusterCredentialsResponse;
import org.springframework.stereotype.Service;

@Service
public class AwsRedshiftCredentialsService {

    public GetClusterCredentialsResponse getCredentials() {
        RedshiftClient redshiftClient = RedshiftClient.builder()
                .region(Region.EU_WEST_2) // or your actual region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        return redshiftClient.getClusterCredentials(GetClusterCredentialsRequest.builder()
                .clusterIdentifier("redshift-cluster-2")
                .dbUser("awsuser") // or IAM-mapped Redshift user
                .autoCreate(false) // change to true if you want auto user creation
                .dbName("dev")
                .build());
    }
}
