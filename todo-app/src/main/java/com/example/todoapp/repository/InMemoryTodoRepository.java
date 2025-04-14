package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTodoRepository implements TodoRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryTodoRepository.class);
    
    private final Map<Long, Todo> todos = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public List<Todo> findAll() {
        logger.info("Finding all todos from in-memory storage");
        return new ArrayList<>(todos.values());
    }

    @Override
    public Optional<Todo> findById(Long id) {
        logger.info("Finding todo by id: {} from in-memory storage", id);
        return Optional.ofNullable(todos.get(id));
    }

    @Override
    public Todo save(Todo todo) {
        logger.info("Saving new todo to in-memory storage");
        Long id = idCounter.getAndIncrement();
        todo.setId(id);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        todos.put(id, todo);
        return todo;
    }

    @Override
    public Todo update(Todo todo) {
        logger.info("Updating todo with id: {} in in-memory storage", todo.getId());
        if (todo.getId() == null || !todos.containsKey(todo.getId())) {
            throw new IllegalArgumentException("Todo not found with id: " + todo.getId());
        }
        
        todo.setUpdatedAt(LocalDateTime.now());
        todos.put(todo.getId(), todo);
        return todo;
    }

    @Override
    public boolean deleteById(Long id) {
        logger.info("Deleting todo with id: {} from in-memory storage", id);
        return todos.remove(id) != null;
    }
}
