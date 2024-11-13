package com.AP2.jenkinsApi.Service;

import com.AP2.jenkinsApi.Controller.DatabaseClientController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MetricsCalculatorServiceTest {

    @Mock
    private DatabaseClientController databaseClient;

    @InjectMocks
    private MetricsCalculatorService metricsCalculator;

    private final String jobName = "TestJob";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateDeploymentFrequency() throws Exception {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(databaseClient.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock result COUNT(*)
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);  // Example: 5 deployments in the last week

        int frequency = metricsCalculator.calculateDeploymentFrequency(jobName);
        assertEquals(5, frequency);

        verify(mockPreparedStatement).setString(1, jobName);
        verify(mockPreparedStatement).setLong(eq(2), anyLong());
    }

    @Test
    void testCalculateLeadTimeForChanges() throws Exception {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(databaseClient.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        java.sql.Timestamp commitTimestamp = java.sql.Timestamp.from(Instant.now().minusSeconds(10 * 60));
        java.sql.Timestamp deployTimestamp = java.sql.Timestamp.from(Instant.now());

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getTimestamp("first_commit")).thenReturn(commitTimestamp);
        when(mockResultSet.getTimestamp("last_deployment")).thenReturn(deployTimestamp);

        long leadTime = metricsCalculator.calculateLeadTimeForChanges(jobName);
        assertEquals(10, leadTime);  // Expected 10 minutes

        verify(mockPreparedStatement).setString(1, jobName);
    }

    @Test
    void testCalculateChangeFailureRate() throws Exception {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(databaseClient.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, true, true, false);
        when(mockResultSet.getString("status")).thenReturn("SUCCESS", "FAILURE", "FAILURE", "SUCCESS");

        double failureRate = metricsCalculator.calculateChangeFailureRate(jobName);
        assertEquals(50.0, failureRate, 0.1);

        verify(mockPreparedStatement).setString(1, jobName);
    }

    @Test
    void testCalculateTimeToRestoreService() throws Exception {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(databaseClient.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        java.sql.Timestamp incidentStart = java.sql.Timestamp.from(Instant.now().minusSeconds(30 * 60));
        java.sql.Timestamp restoreTime = java.sql.Timestamp.from(Instant.now());

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getTimestamp(1)).thenReturn(incidentStart);
        when(mockResultSet.getTimestamp(2)).thenReturn(restoreTime);

        long timeToRestore = metricsCalculator.calculateTimeToRestoreService(jobName);
        assertEquals(30, timeToRestore);  // Expected 30 minutes

        verify(mockPreparedStatement).setString(1, jobName);
    }
}
