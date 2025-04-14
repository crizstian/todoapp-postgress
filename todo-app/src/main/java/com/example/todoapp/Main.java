package com.example.todoapp;

import com.example.todoapp.config.SplitConfig;
import com.example.todoapp.config.StorageManager;
import com.example.todoapp.config.StorageTypeFlag;
import com.example.todoapp.config.UserTypeFlag;
import com.example.todoapp.controller.TodoController;
import com.example.todoapp.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Initialize Split.io client
            SplitConfig.getInstance();
            
            // Initialize storage manager
            StorageManager storageManager = new StorageManager();
            
            // Initialize service and controller with initial repository
            TodoService todoService = new TodoService(storageManager.getCurrentRepository());
            TodoController todoController = new TodoController(todoService);
            
            // Start the CLI
            startCLI(todoController, storageManager);
            
            // Cleanup
            SplitConfig.shutdown();
            storageManager.cleanup();
            
        } catch (Exception e) {
            logger.error("Error starting application", e);
            System.err.println("Error starting application: " + e.getMessage());
        }
    }
    
    private static void startCLI(TodoController todoController, StorageManager storageManager) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        System.out.println("Welcome to Todo App!");
        
        while (running) {
            // Check for feature flag changes before showing the menu
            if (storageManager.updateRepositoryBasedOnFeatureFlag()) {
                // Update the service with the new repository if the storage changed
                todoController.updateRepository(storageManager.getCurrentRepository());
            }
            
            // Also check for user type changes
            storageManager.updateUserTypeBasedOnFeatureFlag();
            
            // Display menu based on user type
            displayMenu(storageManager.isAdminUser());
            
            System.out.print("\nEnter command number: ");
            String input = scanner.nextLine();
            
            try {
                // Process command based on user type
                if (processCommand(input, scanner, todoController, storageManager)) {
                    running = false;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.error("Error processing command", e);
            }
        }
        
        scanner.close();
    }
    
    private static void displayMenu(boolean isAdmin) {
        System.out.println("\nAvailable commands:");
        System.out.println("1. List all todos");
        
        // Only show these options to admin users
        if (isAdmin) {
            System.out.println("2. Get todo by ID");
            System.out.println("3. Create new todo");
            System.out.println("4. Update todo");
            System.out.println("5. Delete todo");
            System.out.println("6. Check storage mode");
        }
        
        System.out.println("7. Update user attributes");
        System.out.println("8. Exit");
    }
    
    private static boolean processCommand(String input, Scanner scanner, 
                                       TodoController todoController, 
                                       StorageManager storageManager) {
        boolean isAdmin = storageManager.isAdminUser();
        
        switch (input) {
            case "1":
                todoController.listAllTodos();
                break;
            case "2":
                if (isAdmin) {
                    System.out.print("Enter todo ID: ");
                    long id = Long.parseLong(scanner.nextLine());
                    todoController.getTodoById(id);
                } else {
                    System.out.println("Access denied. Admin privileges required.");
                }
                break;
            case "3":
                if (isAdmin) {
                    System.out.print("Enter todo title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter todo description: ");
                    String description = scanner.nextLine();
                    todoController.createTodo(title, description);
                } else {
                    System.out.println("Access denied. Admin privileges required.");
                }
                break;
            case "4":
                if (isAdmin) {
                    System.out.print("Enter todo ID to update: ");
                    long updateId = Long.parseLong(scanner.nextLine());
                    System.out.print("Enter new title: ");
                    String newTitle = scanner.nextLine();
                    System.out.print("Enter new description: ");
                    String newDescription = scanner.nextLine();
                    System.out.print("Is completed (true/false): ");
                    boolean completed = Boolean.parseBoolean(scanner.nextLine());
                    todoController.updateTodo(updateId, newTitle, newDescription, completed);
                } else {
                    System.out.println("Access denied. Admin privileges required.");
                }
                break;
            case "5":
                if (isAdmin) {
                    System.out.print("Enter todo ID to delete: ");
                    long deleteId = Long.parseLong(scanner.nextLine());
                    todoController.deleteTodo(deleteId);
                } else {
                    System.out.println("Access denied. Admin privileges required.");
                }
                break;
            case "6":
                if (isAdmin) {
                    boolean isPostgres = StorageTypeFlag.shouldUsePostgresStorage(storageManager.getUserAttributes());
                    System.out.println("Current storage mode: " + 
                            (isPostgres ? "PostgreSQL" : "In-Memory"));
                    System.out.println("Feature flag '" + StorageTypeFlag.getStorageFlagName() + 
                            "' is " + (isPostgres ? "ON" : "OFF"));
                    System.out.println("Current user attributes: " + storageManager.getUserAttributes());
                } else {
                    System.out.println("Access denied. Admin privileges required.");
                }
                break;
            case "7":
                updateUserAttributes(scanner, storageManager, todoController);
                break;
            case "8":
                System.out.println("Exiting application. Goodbye!");
                return true;
            default:
                System.out.println("Invalid command. Please try again.");
        }
        
        return false;
    }
    
    private static void updateUserAttributes(Scanner scanner, StorageManager storageManager, TodoController todoController) {
        System.out.println("\n=== Update User Attributes ===");
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();
        
        System.out.print("Enter user role (admin/user): ");
        String role = scanner.nextLine();
        
        System.out.print("Enter user region: ");
        String region = scanner.nextLine();
        
        Map<String, Object> attributes = SplitConfig.createUserAttributes(userId, role, region);
        
        // Update attributes and check if repository changed
        boolean changed = storageManager.updateUserAttributes(attributes);
        
        if (changed) {
            todoController.updateRepository(storageManager.getCurrentRepository());
            System.out.println("User settings changed based on new attributes.");
        }
        
        System.out.println("User attributes updated successfully.");
        System.out.println("User type: " + (storageManager.isAdminUser() ? "Admin" : "Regular User"));
    }
}
