package com.AP2.jenkinsApi.Controller;

import static org.junit.jupiter.api.Assertions.*;

import com.AP2.jenkinsApi.AppProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class DatabaseClientControllerTest {

    AppProperties appProperties = new AppProperties();

    String dbUrl = appProperties.getProperty("jdbc.url");
    String dbUser = appProperties.getProperty("jdbc.user");
    String dbPassword = appProperties.getProperty("jdbc.password");

    private DatabaseClientController databaseClientController;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    public void setUp() throws SQLException {
        databaseClientController = new DatabaseClientController(dbUrl, dbUser, dbPassword);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS job_status (job_name VARCHAR(255), status VARCHAR(255), created_at TIMESTAMP)")) {
            preparedStatement.execute();
        }
    }



    @Test
    public void testSaveJobStatus() throws SQLException, ParseException {
        String jobName = "TestJenkinsJob";
        String status = "SUCCESS";
        String dateTimeString = "2021-08-01 21:29:40";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long timestamp = dateFormat.parse(dateTimeString).getTime();

        databaseClientController.saveJobStatus(jobName, status, timestamp);

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM job_status WHERE job_name = ?")) {
            preparedStatement.setString(1, jobName);
            ResultSet resultSet = preparedStatement.executeQuery();

            assertTrue(resultSet.next());
            assertEquals(jobName, resultSet.getString("job_name"));
            assertEquals(status, resultSet.getString("status"));
            assertEquals(dateTimeString, resultSet.getString("created_at"));
        }
    }
}