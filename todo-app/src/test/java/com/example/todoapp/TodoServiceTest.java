package com.example.todoapp;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    private TodoService todoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        todoService = new TodoService(todoRepository);
    }

    @Test
    public void testGetAllTodos() {
        // Arrange
        Todo todo1 = new Todo(1L, "Task 1", "Description 1", false, LocalDateTime.now(), LocalDateTime.now());
        Todo todo2 = new Todo(2L, "Task 2", "Description 2", true, LocalDateTime.now(), LocalDateTime.now());
        List<Todo> expectedTodos = Arrays.asList(todo1, todo2);
        
        when(todoRepository.findAll()).thenReturn(expectedTodos);

        // Act
        List<Todo> actualTodos = todoService.getAllTodos();

        // Assert
        assertEquals(expectedTodos, actualTodos);
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    public void testGetTodoById() {
        // Arrange
        Long todoId = 1L;
        Todo expectedTodo = new Todo(todoId, "Task 1", "Description 1", false, LocalDateTime.now(), LocalDateTime.now());
        
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(expectedTodo));

        // Act
        Optional<Todo> actualTodo = todoService.getTodoById(todoId);

        // Assert
        assertTrue(actualTodo.isPresent());
        assertEquals(expectedTodo, actualTodo.get());
        verify(todoRepository, times(1)).findById(todoId);
    }

    @Test
    public void testCreateTodo() {
        // Arrange
        String title = "New Task";
        String description = "New Description";
        
        Todo savedTodo = new Todo(1L, title, description, false, LocalDateTime.now(), LocalDateTime.now());
        
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // Act
        Todo createdTodo = todoService.createTodo(title, description);

        // Assert
        assertNotNull(createdTodo);
        assertEquals(title, createdTodo.getTitle());
        assertEquals(description, createdTodo.getDescription());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    public void testCreateTodoWithEmptyTitle() {
        // Arrange
        String title = "";
        String description = "Description";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.createTodo(title, description);
        });
        
        assertEquals("Todo title cannot be empty", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodo() {
        // Arrange
        Long todoId = 1L;
        String newTitle = "Updated Task";
        String newDescription = "Updated Description";
        boolean completed = true;
        
        Todo existingTodo = new Todo(todoId, "Old Task", "Old Description", false, LocalDateTime.now(), LocalDateTime.now());
        Todo updatedTodo = new Todo(todoId, newTitle, newDescription, completed, existingTodo.getCreatedAt(), LocalDateTime.now());
        
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.update(any(Todo.class))).thenReturn(updatedTodo);

        // Act
        Todo result = todoService.updateTodo(todoId, newTitle, newDescription, completed);

        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertTrue(result.isCompleted());
        verify(todoRepository, times(1)).findById(todoId);
        verify(todoRepository, times(1)).update(any(Todo.class));
    }

    @Test
    public void testUpdateNonExistentTodo() {
        // Arrange
        Long todoId = 999L;
        String newTitle = "Updated Task";
        String newDescription = "Updated Description";
        boolean completed = true;
        
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.updateTodo(todoId, newTitle, newDescription, completed);
        });
        
        assertEquals("Todo not found with id: " + todoId, exception.getMessage());
        verify(todoRepository, times(1)).findById(todoId);
        verify(todoRepository, never()).update(any(Todo.class));
    }

    @Test
    public void testDeleteTodo() {
        // Arrange
        Long todoId = 1L;
        when(todoRepository.deleteById(todoId)).thenReturn(true);

        // Act
        boolean result = todoService.deleteTodo(todoId);

        // Assert
        assertTrue(result);
        verify(todoRepository, times(1)).deleteById(todoId);
    }

    @Test
    public void testDeleteNonExistentTodo() {
        // Arrange
        Long todoId = 999L;
        when(todoRepository.deleteById(todoId)).thenReturn(false);

        // Act
        boolean result = todoService.deleteTodo(todoId);

        // Assert
        assertFalse(result);
        verify(todoRepository, times(1)).deleteById(todoId);
    }
}
