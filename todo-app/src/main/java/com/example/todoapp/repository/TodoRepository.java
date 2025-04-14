package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    List<Todo> findAll();
    Optional<Todo> findById(Long id);
    Todo save(Todo todo);
    Todo update(Todo todo);
    boolean deleteById(Long id);
}
