package com.example.todoapp.config;

import io.split.client.SplitClient;
import io.split.client.api.SplitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Map;

public class UserTypeFlag {
    private static final Logger logger = LoggerFactory.getLogger(UserTypeFlag.class);
    private static final String USER_TYPE_FLAG_NAME = "user_type";

    private UserTypeFlag() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if the user has admin privileges based on the user_type feature flag
     * The feature flag returns a configuration with the user's role
     * 
     * @param attributes Map of user attributes to send to Split.io
     * @return true if user has admin privileges, false otherwise
     */
    public static boolean isAdminUser(Map<String, Object> attributes) {
        boolean isAdmin = false;
        String role = "unknown";
        
        try {
            SplitClient client = SplitConfig.getInstance();
            
            // Get treatment with configuration
            SplitResult result = client.getTreatmentWithConfig(SplitConfig.getSplitUserId(), USER_TYPE_FLAG_NAME, attributes);
            
            // Get treatment and config using reflection to handle different SDK versions
            String treatment = getValueFromSplitResult(result, "treatment");
            String config = getValueFromSplitResult(result, "config");
            
            logger.info("Split.io returned treatment: {} with config: {}", treatment, config);
            
            // Parse the configuration if available
            if (config != null && !config.isEmpty()) {
                try (JsonReader jsonReader = Json.createReader(new StringReader(config))) {
                    JsonObject jsonObject = jsonReader.readObject();
                    
                    if (jsonObject.containsKey("role")) {
                        role = jsonObject.getString("role");
                        isAdmin = "admin".equalsIgnoreCase(role);
                    }
                } catch (Exception e) {
                    logger.error("Error parsing JSON configuration: {}", e.getMessage());
                }
            }
            
            // If no role was found in the config, fall back to treatment-based evaluation
            if ("unknown".equals(role)) {
                isAdmin = "on".equals(treatment);
                role = isAdmin ? "admin" : "user";
                logger.info("No role found in config, falling back to treatment: {}", treatment);
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating user type from Split.io: {}", e.getMessage());
            
            // Fallback to direct attribute check if Split.io fails
            if (attributes != null && attributes.containsKey("role")) {
                role = String.valueOf(attributes.get("role"));
                isAdmin = "admin".equalsIgnoreCase(role);
                logger.info("Fallback to direct attribute check: role is '{}', isAdmin: {}", role, isAdmin);
            }
        }
        
        logger.info("User access determination: role is '{}', isAdmin: {}", role, isAdmin);
        
        return isAdmin;
    }
    
    /**
     * Helper method to safely get values from SplitResult using reflection
     * Works with different versions of the Split.io SDK
     */
    private static String getValueFromSplitResult(SplitResult result, String fieldName) {
        try {
            // First try to access as a method (newer SDK versions)
            try {
                Method method = result.getClass().getMethod(fieldName);
                return (String) method.invoke(result);
            } catch (NoSuchMethodException e) {
                // If method doesn't exist, try as a field (older SDK versions)
                try {
                    java.lang.reflect.Field field = result.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return (String) field.get(result);
                } catch (NoSuchFieldException ex) {
                    logger.warn("Could not access {} in SplitResult", fieldName);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.warn("Error accessing {} in SplitResult: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * Get the user type feature flag name
     */
    public static String getUserTypeFlagName() {
        return USER_TYPE_FLAG_NAME;
    }
}
