package com.example;

public record RedshiftConnectionDetails(
    String jdbcUrl,
    String username,
    String password
) {}
