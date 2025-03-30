package com.example;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class RefreshableDataSource implements DataSource {
    private final AtomicReference<HikariDataSource> dataSourceRef;

    public RefreshableDataSource(HikariDataSource initialDataSource) {
        this.dataSourceRef = new AtomicReference<>(initialDataSource);
    }

    public void refresh(HikariDataSource newDataSource) {
        HikariDataSource oldDataSource = dataSourceRef.getAndSet(newDataSource);
        if (oldDataSource != null && !oldDataSource.isClosed()) {
            oldDataSource.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSourceRef.get().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSourceRef.get().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSourceRef.get().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSourceRef.get().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSourceRef.get().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSourceRef.get().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSourceRef.get().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSourceRef.get().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSourceRef.get().isWrapperFor(iface);
    }
}