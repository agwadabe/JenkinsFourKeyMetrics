package com.AP2.jenkinsApi.Controller;

import java.sql.*;
import java.text.SimpleDateFormat;

public class DatabaseClientController {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseClientController(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void saveJobStatus(String job_name, String status, Long created_at) throws SQLException {
        String query = "INSERT INTO job_status (job_name, status, created_at) VALUES (?, ?, ?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = sdf.format(new Date(created_at));

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, job_name);
            pstmt.setString(2, status);
            pstmt.setString(3, formattedTimestamp);

            pstmt.executeUpdate();
        }
    }

    public void saveDeployment(String job_name, String status, long timestamp) throws SQLException {
        String query = "INSERT INTO deployments (job_name, status, timestamp) VALUES (?, ?, ?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = sdf.format(new Date(timestamp));
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, job_name);
            pstmt.setString(2, status);
            pstmt.setString(3, formattedTimestamp);

            pstmt.executeUpdate();
        }
    }

    public void saveIncident(String job_name, String severity, String description, long timestamp, long incident_start, long restore_time) throws SQLException {
        String query = "INSERT INTO incidents (job_name, severity, description, timestamp, incident_start, restore_time) VALUES (?, ?, ?, ?, ?, ?)";

        Timestamp sqlTimestamp = new Timestamp(timestamp);
        Timestamp sqlIncidentStart = new Timestamp(incident_start);
        Timestamp sqlRestoreTime = (restore_time != 0) ? new Timestamp(restore_time) : null;
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, job_name);
            pstmt.setString(2, severity);
            pstmt.setString(3, description);
            pstmt.setTimestamp(4, sqlTimestamp);
            pstmt.setTimestamp(5, sqlIncidentStart);

            if (sqlRestoreTime != null) {
                pstmt.setTimestamp(6, sqlRestoreTime);
            } else {
                pstmt.setNull(6, java.sql.Types.TIMESTAMP);
            }

            pstmt.executeUpdate();
        }
    }
    public void saveJobCommit(String jobName, long commitTimestamp, long deploymentTimestamp) throws SQLException {
        String query = "INSERT INTO job_commits (job_name, commit_timestamp, deployment_timestamp) VALUES (?, ?, ?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedCommitTimestamp = sdf.format(new Date(commitTimestamp));
        String formattedDeploymentTimestamp = sdf.format(new Date(deploymentTimestamp));

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, jobName);
            pstmt.setString(2, formattedCommitTimestamp);
            pstmt.setString(3, formattedDeploymentTimestamp);

            pstmt.executeUpdate();
        }
    }

}