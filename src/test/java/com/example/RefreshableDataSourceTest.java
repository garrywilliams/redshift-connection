package com.example;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshableDataSourceTest {

    @Mock
    private HikariDataSource initialDataSource;

    @Mock
    private HikariDataSource newDataSource;

    @Mock
    private Connection mockConnection;

    private RefreshableDataSource refreshableDataSource;

    @BeforeEach
    public void setup() {
        refreshableDataSource = new RefreshableDataSource(initialDataSource);
    }

    @Test
    void shouldDelegateGetConnectionToUnderlyingDataSource() throws SQLException {
        // Given
        when(initialDataSource.getConnection()).thenReturn(mockConnection);

        // When
        Connection connection = refreshableDataSource.getConnection();

        // Then
        assertSame(mockConnection, connection);
        verify(initialDataSource).getConnection();
    }

    @Test
    void shouldRefreshDataSourceAndCloseOldOne() throws SQLException {
        // Given
        when(initialDataSource.isClosed()).thenReturn(false);

        // When
        refreshableDataSource.refresh(newDataSource);

        // Then
        verify(initialDataSource).close();

        // Verify the new data source is used for subsequent operations
        when(newDataSource.getConnection()).thenReturn(mockConnection);
        Connection connection = refreshableDataSource.getConnection();
        assertSame(mockConnection, connection);
        verify(newDataSource).getConnection();
    }

    @Test
    void shouldNotCloseAlreadyClosedDataSource() throws SQLException {
        // Given
        when(initialDataSource.isClosed()).thenReturn(true);

        // When
        refreshableDataSource.refresh(newDataSource);

        // Then
        verify(initialDataSource, never()).close();
    }
}
