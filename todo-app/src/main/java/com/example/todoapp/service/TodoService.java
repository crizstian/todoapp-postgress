package com.example.todoapp.service;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);
    
    private TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }
    
    /**
     * Updates the repository used by this service
     */
    public void updateRepository(TodoRepository repository) {
        logger.info("Switching to new repository: {}", repository.getClass().getSimpleName());
        this.todoRepository = repository;
    }

    public List<Todo> getAllTodos() {
        logger.info("Getting all todos");
        return todoRepository.findAll();
    }

    public Optional<Todo> getTodoById(Long id) {
        logger.info("Getting todo by id: {}", id);
        return todoRepository.findById(id);
    }

    public Todo createTodo(String title, String description) {
        logger.info("Creating new todo with title: {}", title);
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo title cannot be empty");
        }
        
        Todo todo = new Todo(title, description);
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long id, String title, String description, boolean completed) {
        logger.info("Updating todo with id: {}", id);
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo title cannot be empty");
        }
        
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id));
        
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setCompleted(completed);
        
        return todoRepository.update(todo);
    }

    public boolean deleteTodo(Long id) {
        logger.info("Deleting todo by id: {}", id);
        return todoRepository.deleteById(id);
    }
}
