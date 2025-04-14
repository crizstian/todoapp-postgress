package com.example.todoapp.config;

import io.split.client.SplitClient;
import io.split.client.SplitClientConfig;
import io.split.client.SplitFactory;
import io.split.client.SplitFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class SplitConfig {
    private static final Logger logger = LoggerFactory.getLogger(SplitConfig.class);
    private static volatile SplitClient instance;
    private static final Object lock = new Object();
    private static final String SPLIT_USER_ID = "user-123";

    private SplitConfig() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the singleton instance of SplitClient.
     * Thread-safe implementation with double-checked locking.
     * 
     * @return The SplitClient singleton instance
     */
    public static SplitClient getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    try {
                        instance = initializeSplitClient();
                        logger.info("Split.io client singleton initialized successfully");
                    } catch (Exception e) {
                        logger.error("Failed to initialize Split.io client", e);
                        throw new RuntimeException("Failed to initialize Split.io client", e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Get default user attributes for Split.io
     * 
     * @return Map of default attributes
     */
    public static Map<String, Object> getDefaultAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("environment", "development");
        attributes.put("appVersion", "1.0.0");
        attributes.put("platform", System.getProperty("os.name"));
        attributes.put("role", "user"); // Default role is non-admin
        return attributes;
    }

    /**
     * Create user attributes for a specific user
     * 
     * @param userId User identifier
     * @param role User role (e.g., "admin", "user")
     * @param region User region
     * @return Map of user attributes
     */
    public static Map<String, Object> createUserAttributes(String userId, String role, String region) {
        Map<String, Object> attributes = getDefaultAttributes();
        attributes.put("userId", userId);
        attributes.put("role", role);
        attributes.put("region", region);
        return attributes;
    }

    /**
     * Get the user ID used for Split.io treatments
     */
    public static String getSplitUserId() {
        return SPLIT_USER_ID;
    }

    private static SplitClient initializeSplitClient() 
            throws IOException, TimeoutException, InterruptedException, URISyntaxException {
        Properties properties = loadProperties();
        String apiKey = properties.getProperty("split.api.key");
        
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_SPLIT_API_KEY".equals(apiKey)) {
            throw new IOException("Split.io API key not configured properly. Please set a valid API key in application.properties.");
        }
        
        SplitClientConfig config = SplitClientConfig.builder()
                .setBlockUntilReadyTimeout(10000)
                .build();
        
        SplitFactory splitFactory = SplitFactoryBuilder.build(apiKey, config);
        SplitClient client = splitFactory.client();
        
        // Block until the client is ready or times out
        client.blockUntilReady();
        
        return client;
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = SplitConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
        }
        return properties;
    }

    /**
     * Shutdown the Split.io client properly
     */
    public static void shutdown() {
        if (instance != null) {
            instance.destroy();
            instance = null;
            logger.info("Split.io client shutdown successfully");
        }
    }
}
