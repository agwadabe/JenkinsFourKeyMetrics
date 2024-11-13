package com.AP2.jenkinsApi.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class JenkinsClientController {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String jenkinsUrl;
    private final String user;
    private final String token;

    public JenkinsClientController(String jenkinsUrl, String user, String token) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.jenkinsUrl = jenkinsUrl;
        this.user = user;
        this.token = token;
    }

    public JsonNode getJobStatuses() throws IOException {
        String url = jenkinsUrl + "/api/json?tree=jobs[name,lastBuild[result]]";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", okhttp3.Credentials.basic(user, token))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            return objectMapper.readTree(responseBody);
        }
    }

    private JsonNode makeApiRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", okhttp3.Credentials.basic(user, token))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();

            return objectMapper.readTree(responseBody);
        }
    }


    public String getLastSuccessfulBuildTimestamp(String jobName) throws Exception {
        String url = jenkinsUrl + "/job/" + jobName + "/lastSuccessfulBuild/api/json";

        JsonNode buildData = makeApiRequest(url);

        if (buildData != null && buildData.has("timestamp")) {
            return buildData.get("timestamp").asText(); // Return as String for easy parsing later
        } else {
            throw new Exception("No timestamp found for the last successful build of job: " + jobName);
        }
    }





}
