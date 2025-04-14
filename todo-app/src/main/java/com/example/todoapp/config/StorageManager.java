package com.example.todoapp.config;

import com.example.todoapp.repository.InMemoryTodoRepository;
import com.example.todoapp.repository.PostgresTodoRepository;
import com.example.todoapp.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class StorageManager {
    private static final Logger logger = LoggerFactory.getLogger(StorageManager.class);
    
    private TodoRepository currentRepository;
    private TodoRepository postgresRepository;
    private TodoRepository inMemoryRepository;
    private Connection connection;
    private Map<String, Object> userAttributes;
    private boolean isAdminUser;
    
    public StorageManager() throws SQLException {
        // Initialize both repositories
        connection = DatabaseConfig.getConnection();
        postgresRepository = new PostgresTodoRepository(connection);
        inMemoryRepository = new InMemoryTodoRepository();
        
        // Set default user attributes
        userAttributes = SplitConfig.getDefaultAttributes();
        
        // Set the initial repository based on feature flag
        updateRepositoryBasedOnFeatureFlag();
        
        // Check if user is admin
        updateUserTypeBasedOnFeatureFlag();
    }
    
    /**
     * Updates the current repository based on the feature flag status
     * @return true if the repository was changed, false otherwise
     */
    public boolean updateRepositoryBasedOnFeatureFlag() {
        boolean usePostgres = StorageTypeFlag.shouldUsePostgresStorage(userAttributes);
        TodoRepository newRepository = usePostgres ? postgresRepository : inMemoryRepository;
        
        if (currentRepository != newRepository) {
            logger.info("Switching storage from {} to {}", 
                    currentRepository == null ? "none" : 
                    (currentRepository == postgresRepository ? "PostgreSQL" : "in-memory"),
                    usePostgres ? "PostgreSQL" : "in-memory");
            
            currentRepository = newRepository;
            return true;
        }
        
        return false;
    }
    
    /**
     * Updates user type based on the feature flag status
     * @return true if the user type was changed, false otherwise
     */
    public boolean updateUserTypeBasedOnFeatureFlag() {
        boolean newIsAdmin = UserTypeFlag.isAdminUser(userAttributes);
        
        if (isAdminUser != newIsAdmin) {
            logger.info("User type changed from {} to {}", 
                    isAdminUser ? "admin" : "regular user",
                    newIsAdmin ? "admin" : "regular user");
            
            isAdminUser = newIsAdmin;
            return true;
        }
        
        return false;
    }
    
    /**
     * Updates user attributes and checks if repository or user type needs to be changed
     * @param attributes New user attributes
     * @return true if any changes occurred, false otherwise
     */
    public boolean updateUserAttributes(Map<String, Object> attributes) {
        this.userAttributes = attributes;
        logger.info("Updated user attributes: {}", attributes);
        
        boolean repositoryChanged = updateRepositoryBasedOnFeatureFlag();
        boolean userTypeChanged = updateUserTypeBasedOnFeatureFlag();
        
        return repositoryChanged || userTypeChanged;
    }
    
    /**
     * Gets the current user attributes
     */
    public Map<String, Object> getUserAttributes() {
        return userAttributes;
    }
    
    /**
     * Checks if the current user is an admin
     */
    public boolean isAdminUser() {
        return isAdminUser;
    }
    
    /**
     * Gets the current repository based on the feature flag status
     */
    public TodoRepository getCurrentRepository() {
        return currentRepository;
    }
    
    /**
     * Closes database connections and performs cleanup
     */
    public void cleanup() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
}
