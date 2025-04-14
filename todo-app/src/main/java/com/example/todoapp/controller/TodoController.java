package com.example.todoapp.controller;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TodoController {
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);
    
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }
    
    /**
     * Updates the repository used by the service
     */
    public void updateRepository(TodoRepository repository) {
        logger.info("Updating repository in TodoController");
        todoService.updateRepository(repository);
    }

    public void listAllTodos() {
        logger.info("Listing all todos");
        List<Todo> todos = todoService.getAllTodos();
        
        if (todos.isEmpty()) {
            System.out.println("No todos found.");
            return;
        }
        
        System.out.println("\n===== All Todos =====");
        for (Todo todo : todos) {
            printTodo(todo);
        }
    }

    public void getTodoById(Long id) {
        logger.info("Getting todo by id: {}", id);
        Optional<Todo> todoOptional = todoService.getTodoById(id);
        
        if (todoOptional.isPresent()) {
            System.out.println("\n===== Todo Details =====");
            printTodo(todoOptional.get());
        } else {
            System.out.println("Todo not found with id: " + id);
        }
    }

    public void createTodo(String title, String description) {
        logger.info("Creating new todo with title: {}", title);
        try {
            Todo todo = todoService.createTodo(title, description);
            System.out.println("Todo created successfully:");
            printTodo(todo);
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating todo: " + e.getMessage());
        }
    }

    public void updateTodo(Long id, String title, String description, boolean completed) {
        logger.info("Updating todo with id: {}", id);
        try {
            Todo todo = todoService.updateTodo(id, title, description, completed);
            System.out.println("Todo updated successfully:");
            printTodo(todo);
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating todo: " + e.getMessage());
        }
    }

    public void deleteTodo(Long id) {
        logger.info("Deleting todo with id: {}", id);
        boolean deleted = todoService.deleteTodo(id);
        
        if (deleted) {
            System.out.println("Todo deleted successfully with id: " + id);
        } else {
            System.out.println("Todo not found with id: " + id);
        }
    }
    
    private void printTodo(Todo todo) {
        System.out.println("ID: " + todo.getId());
        System.out.println("Title: " + todo.getTitle());
        System.out.println("Description: " + todo.getDescription());
        System.out.println("Status: " + (todo.isCompleted() ? "Completed" : "Pending"));
        System.out.println("Created: " + todo.getCreatedAt());
        System.out.println("Updated: " + todo.getUpdatedAt());
        System.out.println("------------------------");
    }
}
