package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresTodoRepository implements TodoRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresTodoRepository.class);
    
    private final Connection connection;

    public PostgresTodoRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Todo> findAll() {
        logger.info("Finding all todos from PostgreSQL");
        List<Todo> todos = new ArrayList<>();
        
        String sql = "SELECT * FROM todos";
        
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                todos.add(mapResultSetToTodo(resultSet));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all todos", e);
            throw new RuntimeException("Error finding all todos", e);
        }
        
        return todos;
    }

    @Override
    public Optional<Todo> findById(Long id) {
        logger.info("Finding todo by id: {} from PostgreSQL", id);
        String sql = "SELECT * FROM todos WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToTodo(resultSet));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding todo by id: {}", id, e);
            throw new RuntimeException("Error finding todo by id: " + id, e);
        }
        
        return Optional.empty();
    }

    @Override
    public Todo save(Todo todo) {
        logger.info("Saving new todo to PostgreSQL");
        String sql = "INSERT INTO todos (title, description, completed) VALUES (?, ?, ?) RETURNING id, created_at, updated_at";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, todo.getTitle());
            statement.setString(2, todo.getDescription());
            statement.setBoolean(3, todo.isCompleted());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    todo.setId(resultSet.getLong("id"));
                    todo.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    todo.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                } else {
                    throw new SQLException("Creating todo failed, no ID obtained.");
                }
            }
            
            return todo;
            
        } catch (SQLException e) {
            logger.error("Error saving todo", e);
            throw new RuntimeException("Error saving todo", e);
        }
    }

    @Override
    public Todo update(Todo todo) {
        logger.info("Updating todo with id: {} in PostgreSQL", todo.getId());
        String sql = "UPDATE todos SET title = ?, description = ?, completed = ? WHERE id = ? RETURNING created_at, updated_at";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, todo.getTitle());
            statement.setString(2, todo.getDescription());
            statement.setBoolean(3, todo.isCompleted());
            statement.setLong(4, todo.getId());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    todo.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    todo.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                } else {
                    throw new SQLException("Updating todo failed, no rows affected.");
                }
            }
            
            return todo;
            
        } catch (SQLException e) {
            logger.error("Error updating todo with id: {}", todo.getId(), e);
            throw new RuntimeException("Error updating todo with id: " + todo.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        logger.info("Deleting todo with id: {} from PostgreSQL", id);
        String sql = "DELETE FROM todos WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.error("Error deleting todo with id: {}", id, e);
            throw new RuntimeException("Error deleting todo with id: " + id, e);
        }
    }
    
    private Todo mapResultSetToTodo(ResultSet resultSet) throws SQLException {
        return new Todo(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getBoolean("completed"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
