package com.example;

public record RedshiftDbCredentials(
    String dbName,
    String dbUser,
    String dbPassword,
    String host,
    int port
) {}
