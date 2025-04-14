package com.example.todoapp.config;

import io.split.client.SplitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StorageTypeFlag {
    private static final Logger logger = LoggerFactory.getLogger(StorageTypeFlag.class);
    private static final String STORAGE_FLAG_NAME = "storage_type";

    private StorageTypeFlag() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if PostgreSQL storage should be used based on the feature flag
     * with default user attributes
     * 
     * @return true if PostgreSQL should be used, false for in-memory storage
     */
    public static boolean shouldUsePostgresStorage() {
        return shouldUsePostgresStorage(SplitConfig.getDefaultAttributes());
    }

    /**
     * Checks if PostgreSQL storage should be used based on the feature flag
     * with custom user attributes
     * 
     * @param attributes Map of user attributes to send to Split.io
     * @return true if PostgreSQL should be used, false for in-memory storage
     */
    public static boolean shouldUsePostgresStorage(Map<String, Object> attributes) {
        SplitClient client = SplitConfig.getInstance();
        String treatment = client.getTreatment(SplitConfig.getSplitUserId(), STORAGE_FLAG_NAME, attributes);
        boolean usePostgres = "on".equals(treatment);
        logger.info("Feature flag '{}' is set to: {} (using PostgreSQL: {}) with attributes: {}", 
                STORAGE_FLAG_NAME, treatment, usePostgres, attributes);
        return usePostgres;
    }

    /**
     * Get the storage feature flag name
     */
    public static String getStorageFlagName() {
        return STORAGE_FLAG_NAME;
    }
}
