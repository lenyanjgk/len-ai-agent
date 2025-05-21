package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作工具类
 */
@Component
public class DatabaseOperationTool {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 查询数据
     */
    @Tool(description = "Query data from database")
    public String queryData(
            @ToolParam(description = "SQL query statement") String sql,
            @ToolParam(description = "Query parameters in JSON format, e.g. {\"id\":1, \"name\":\"test\"}") String params
    ) {
        try {
            if (params == null || params.isEmpty()) {
                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                return formatResultList(result);
            } else {
                Map<String, Object> paramMap = parseParams(params);
                List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(sql, paramMap);
                return formatResultList(result);
            }
        } catch (Exception e) {
            return "Error executing query: " + e.getMessage();
        }
    }

    /**
     * 插入数据
     */
    @Tool(description = "Insert data into database")
    public String insertData(
            @ToolParam(description = "SQL insert statement") String sql,
            @ToolParam(description = "Insert parameters in JSON format") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
            return rowsAffected + " row(s) inserted successfully";
        } catch (Exception e) {
            return "Error inserting data: " + e.getMessage();
        }
    }

    /**
     * 更新数据
     */
    @Tool(description = "Update data in database")
    public String updateData(
            @ToolParam(description = "SQL update statement") String sql,
            @ToolParam(description = "Update parameters in JSON format") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
            return rowsAffected + " row(s) updated successfully";
        } catch (Exception e) {
            return "Error updating data: " + e.getMessage();
        }
    }

    /**
     * 删除数据
     */
    @Tool(description = "Delete data from database")
    public String deleteData(
            @ToolParam(description = "SQL delete statement") String sql,
            @ToolParam(description = "Delete parameters in JSON format") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
            return rowsAffected + " row(s) deleted successfully";
        } catch (Exception e) {
            return "Error deleting data: " + e.getMessage();
        }
    }

    /**
     * 解析JSON参数
     */
    private Map<String, Object> parseParams(String paramsJson) {
        // 这里可以使用JSON库解析参数
        // 简化实现，假设参数格式为 {"key1":"value1", "key2":"value2"}
        Map<String, Object> paramMap = new HashMap<>();
        try {
            if (paramsJson != null && !paramsJson.isEmpty()) {
                // 移除花括号和引号
                String cleanParams = paramsJson.replaceAll("[{}\"]", "");
                String[] pairs = cleanParams.split(",");
                
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim();
                        String value = keyValue[1].trim();
                        paramMap.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            // 解析错误，返回空Map
            return new HashMap<>();
        }
        return paramMap;
    }

    /**
     * 格式化查询结果
     */
    private String formatResultList(List<Map<String, Object>> resultList) {
        if (resultList.isEmpty()) {
            return "No data found";
        }
        
        StringBuilder sb = new StringBuilder();
        // 添加表头
        Map<String, Object> firstRow = resultList.get(0);
        sb.append(String.join(" | ", firstRow.keySet())).append("\n");
        
        // 创建分隔线
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < sb.length() - 1; i++) { // 减1是去掉换行符
            separator.append("-");
        }
        sb.append(separator).append("\n");
        
        // 添加数据行
        for (Map<String, Object> row : resultList) {
            sb.append(row.values().stream()
                    .map(val -> val == null ? "NULL" : val.toString())
                    .reduce((a, b) -> a + " | " + b)
                    .orElse(""))
                    .append("\n");
        }
        
        return sb.toString();
    }
} 