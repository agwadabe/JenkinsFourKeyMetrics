package com.AP2.jenkinsApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class AppProperties {
    private Properties properties;

    public AppProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
