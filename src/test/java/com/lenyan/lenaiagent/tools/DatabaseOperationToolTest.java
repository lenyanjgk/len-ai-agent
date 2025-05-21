package com.lenyan.lenaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DatabaseOperationToolTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private DatabaseOperationTool databaseOperationTool;
    
    // 定义静态帮助方法
    @SuppressWarnings("unchecked")
    private static Map<String, Object> anyMap() {
        return any(Map.class);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testQueryDataWithoutParams() {
        // Arrange
        String sql = "SELECT * FROM users";
        List<Map<String, Object>> mockResult = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("name", "Test User");
        mockResult.add(row);
        
        when(jdbcTemplate.queryForList(sql)).thenReturn(mockResult);

        // Act
        String result = databaseOperationTool.queryData(sql, "");

        // Assert
        verify(jdbcTemplate, times(1)).queryForList(sql);
        assertTrue(result.contains("id | name"));
        assertTrue(result.contains("1 | Test User"));
    }

    @Test
    void testQueryDataWithParams() {
        // Arrange
        String sql = "SELECT * FROM users WHERE id = :id";
        String params = "{\"id\":1}";
        List<Map<String, Object>> mockResult = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("name", "Test User");
        mockResult.add(row);
        
        when(namedParameterJdbcTemplate.queryForList(eq(sql), anyMap())).thenReturn(mockResult);

        // Act
        String result = databaseOperationTool.queryData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).queryForList(eq(sql), anyMap());
        assertTrue(result.contains("id | name"));
        assertTrue(result.contains("1 | Test User"));
    }

    @Test
    void testQueryDataWithException() {
        // Arrange
        String sql = "SELECT * FROM users";
        when(jdbcTemplate.queryForList(sql)).thenThrow(new RuntimeException("Test exception"));

        // Act
        String result = databaseOperationTool.queryData(sql, "");

        // Assert
        verify(jdbcTemplate, times(1)).queryForList(sql);
        assertTrue(result.contains("Error executing query"));
    }

    @Test
    void testQueryDataWithEmptyResult() {
        // Arrange
        String sql = "SELECT * FROM users WHERE id = 999";
        List<Map<String, Object>> emptyResult = new ArrayList<>();
        when(jdbcTemplate.queryForList(sql)).thenReturn(emptyResult);

        // Act
        String result = databaseOperationTool.queryData(sql, "");

        // Assert
        verify(jdbcTemplate, times(1)).queryForList(sql);
        assertEquals("No data found", result);
    }

    @Test
    void testInsertData() {
        // Arrange
        String sql = "INSERT INTO users (name, email) VALUES (:name, :email)";
        String params = "{\"name\":\"New User\", \"email\":\"user@example.com\"}";
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap())).thenReturn(1);

        // Act
        String result = databaseOperationTool.insertData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).update(eq(sql), anyMap());
        assertEquals("1 row(s) inserted successfully", result);
    }

    @Test
    void testInsertDataWithException() {
        // Arrange
        String sql = "INSERT INTO users (name, email) VALUES (:name, :email)";
        String params = "{\"name\":\"New User\", \"email\":\"user@example.com\"}";
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        String result = databaseOperationTool.insertData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).update(eq(sql), anyMap());
        assertTrue(result.contains("Error inserting data"));
    }

    @Test
    void testUpdateData() {
        // Arrange
        String sql = "UPDATE users SET name = :name WHERE id = :id";
        String params = "{\"name\":\"Updated User\", \"id\":1}";
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap())).thenReturn(1);

        // Act
        String result = databaseOperationTool.updateData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).update(eq(sql), anyMap());
        assertEquals("1 row(s) updated successfully", result);
    }

    @Test
    void testUpdateDataWithException() {
        // Arrange
        String sql = "UPDATE users SET name = :name WHERE id = :id";
        String params = "{\"name\":\"Updated User\", \"id\":1}";
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        String result = databaseOperationTool.updateData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).update(eq(sql), anyMap());
        assertTrue(result.contains("Error updating data"));
    }

    @Test
    void testDeleteData() {
        // Arrange
        String sql = "DELETE FROM users WHERE id = :id";
        String params = "{\"id\":1}";
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap())).thenReturn(1);

        // Act
        String result = databaseOperationTool.deleteData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).update(eq(sql), anyMap());
        assertEquals("1 row(s) deleted successfully", result);
    }

    @Test
    void testDeleteDataWithException() {
        // Arrange
        String sql = "DELETE FROM users WHERE id = :id";
        String params = "{\"id\":1}";
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        String result = databaseOperationTool.deleteData(sql, params);

        // Assert
        verify(namedParameterJdbcTemplate, times(1)).update(eq(sql), anyMap());
        assertTrue(result.contains("Error deleting data"));
    }
} 