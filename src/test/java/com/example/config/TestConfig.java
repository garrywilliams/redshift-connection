package com.example.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.sts.StsClient;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @MockBean
    private RedshiftClient redshiftClient;
    
    @MockBean
    private StsClient stsClient;
    
    // No bean definitions here - just use the MockBeans
}
