package com.AP2.jenkinsApi.Controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JenkinsClientControllerTest {

    private MockWebServer mockWebServer;
    private JenkinsClientController jenkinsClient;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        jenkinsClient = new JenkinsClientController(baseUrl, "user", "token");
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetJobStatuses() throws Exception {
        String mockResponse = "{\"jobs\":[{\"name\":\"job1\",\"lastBuild\":{\"result\":\"SUCCESS\"}}]}";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        JsonNode response = jenkinsClient.getJobStatuses();
        assertNotNull(response);
        assertEquals("job1", response.get("jobs").get(0).get("name").asText());
        assertEquals("SUCCESS", response.get("jobs").get(0).get("lastBuild").get("result").asText());
    }
}