package com.AP2.jenkinsApi.Service;

import com.AP2.jenkinsApi.Controller.DatabaseClientController;
import com.AP2.jenkinsApi.Controller.JenkinsClientController;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.*;
import java.time.Instant;

public class MetricsCalculatorService {

    private final String jenkinsUrl;
    private final JenkinsClientController jenkinsClient;
    private final DatabaseClientController databaseClient;

    public MetricsCalculatorService(String jenkinsUrl, JenkinsClientController jenkinsClient, DatabaseClientController databaseClient) {
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsClient = jenkinsClient;
        this.databaseClient = databaseClient;
    }

    public int calculateDeploymentFrequency(String jobName) throws SQLException {
        String query = "SELECT COUNT(*) FROM deployments WHERE job_name = ? AND timestamp >= ?";
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, jobName);
            preparedStatement.setLong(2, Instant.now().minusSeconds(7 * 24 * 3600).toEpochMilli()); // letzte Woche

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);  // Gibt die Anzahl der Deployments zurück
            }
        }
        return 0;
    }

    public long calculateLeadTimeForChanges(String jobName) throws SQLException {
        String query = "SELECT MIN(commit_timestamp) AS first_commit, MAX(deployment_timestamp) AS last_deployment " +
                "FROM job_commits WHERE job_name = ?";

        try (Connection connection = databaseClient.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, jobName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                java.sql.Timestamp commitTimestamp = resultSet.getTimestamp("first_commit");
                java.sql.Timestamp deployTimestamp = resultSet.getTimestamp("last_deployment");

                if (commitTimestamp == null || deployTimestamp == null) {
                    return 0;
                }

                long leadTimeMillis = deployTimestamp.getTime() - commitTimestamp.getTime();
                return leadTimeMillis / (1000 * 60);
            }
        }
        return 0;
    }



    // 3. Change Failure Rate
    public double calculateChangeFailureRate(String jobName) throws SQLException {
        String query = "SELECT status FROM deployments WHERE job_name = ?";
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, jobName);
            ResultSet resultSet = preparedStatement.executeQuery();

            int totalDeployments = 0;
            int failedDeployments = 0;

            while (resultSet.next()) {
                totalDeployments++;
                if ("FAILURE".equals(resultSet.getString("status"))) {
                    failedDeployments++;
                }
            }
            return totalDeployments == 0 ? 0 : (double) failedDeployments / totalDeployments * 100;
        }
    }



    public long calculateTimeToRestoreService(String jobName) throws SQLException, Exception {
        String query = "SELECT MIN(incident_start), MAX(restore_time) FROM incidents WHERE job_name = ?";
        long restoreTimeMillis = 0;

        try (Connection connection = databaseClient.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, jobName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                java.sql.Timestamp incidentStartTimestamp = resultSet.getTimestamp(1);
                java.sql.Timestamp restoreTimeTimestamp = resultSet.getTimestamp(2);

                if (incidentStartTimestamp == null) {
                    return 0;
                }

                long incidentStartMillis = incidentStartTimestamp.getTime();

                if (restoreTimeTimestamp != null) {
                    restoreTimeMillis = restoreTimeTimestamp.getTime();
                } else {
                    String lastSuccessfulBuildTimestamp = jenkinsClient.getLastSuccessfulBuildTimestamp(jobName);

                    if (lastSuccessfulBuildTimestamp != null) {
                        restoreTimeMillis = Long.parseLong(lastSuccessfulBuildTimestamp);
                    } else {
                        throw new Exception("No valid restore time found from incidents or Jenkins.");
                    }
                }

                return (restoreTimeMillis - incidentStartMillis) / (1000 * 60);
            }
        }

        return 0;
    }


}
