package com.AP2.jenkinsApi;

import com.AP2.jenkinsApi.Controller.DatabaseClientController;
import com.AP2.jenkinsApi.Controller.JenkinsClientController;
import com.AP2.jenkinsApi.Service.MetricsCalculatorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;

@SpringBootApplication
public class JenkinsApiApplication {


	public static void main(String[] args) {
		SpringApplication.run(JenkinsApiApplication.class, args);
	
	 AppProperties appProperties = new AppProperties();


	 String jenkinsUrl = appProperties.getProperty("jenkins.url");
     String jenkinsUser = appProperties.getProperty("jenkins.user");
	 String jenkinsToken = appProperties.getProperty("jenkins.token");

	 String dbUrl = appProperties.getProperty("jdbc.url");
	 String dbUser = appProperties.getProperty("jdbc.user");
	 String dbPassword = appProperties.getProperty("jdbc.password");

		JenkinsClientController jenkinsClient = new JenkinsClientController(jenkinsUrl, jenkinsUser, jenkinsToken);
		DatabaseClientController databaseClient = new DatabaseClientController(dbUrl, dbUser, dbPassword);
		MetricsCalculatorService metricsCalculator = new MetricsCalculatorService(jenkinsUrl, jenkinsClient, databaseClient);


		try {
			JsonNode jobs = jenkinsClient.getJobStatuses();
			for (JsonNode job : jobs.get("jobs")) {
				String jobName = job.get("name").asText();
				long timestamp = Instant.now().toEpochMilli();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedTimestamp = sdf.format(new Date(timestamp));

				JsonNode lastBuild = job.get("lastBuild");
				String status = lastBuild != null && lastBuild.has("result") ? lastBuild.get("result").asText() : "UNKNOWN";

				String description = job.has("description") ? job.get("description").asText() : "No description available";
				String severity = "UNDEFINED";

				if (lastBuild != null && lastBuild.has("actions")) {
					for (JsonNode action : lastBuild.get("actions")) {
						if (action.has("parameters")) {
							for (JsonNode parameter : action.get("parameters")) {
								if ("severity".equals(parameter.get("name").asText())) {
									severity = parameter.get("value").asText();
								}
							}
						}
					}
				}

				databaseClient.saveJobStatus(jobName, status, timestamp);
				databaseClient.saveDeployment(jobName, status, timestamp);

				long incidentStart = timestamp;
				long restoreTime = 0;
				databaseClient.saveIncident(jobName, severity, description, timestamp, incidentStart, restoreTime);

				long commitTimestamp = timestamp;
				long deploymentTimestamp = timestamp;
				databaseClient.saveJobCommit(jobName, commitTimestamp, deploymentTimestamp);

	/*
				System.out.println("Job Name: " + jobName);
				System.out.println("Status: " + status);
				System.out.println("timestamp: " + formattedTimestamp);
				System.out.println("----------");


	 */

					System.out.println("Job Name: " + jobName + "\n" + "Deployment Frequency: " + metricsCalculator.calculateDeploymentFrequency("TestJenkinsJob"));
					System.out.println("Lead Time for Changes: " + metricsCalculator.calculateLeadTimeForChanges("TestJenkinsJob") + " Minuten");
				System.out.println("Change Failure Rate: " + metricsCalculator.calculateChangeFailureRate("TestJenkinsJob"));
					System.out.println("Time to Restore Service: " + metricsCalculator.calculateTimeToRestoreService("TestJenkinsJob") + " Minuten");
				System.out.println("----------");


				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
