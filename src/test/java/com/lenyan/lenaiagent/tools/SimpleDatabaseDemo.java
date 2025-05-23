package com.lenyan.lenaiagent.tools;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 简单数据库操作演示
 * 这个类不依赖Spring Boot，可以直接运行
 */
public class SimpleDatabaseDemo {
    
    public static void main(String[] args) {
        // 创建数据源 - 请根据实际情况修改数据库连接信息
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/lenai?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        
        // 创建JdbcTemplate和NamedParameterJdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        // 创建数据库操作工具
        DatabaseOperationTool databaseTool = new DatabaseOperationTool();
        
        // 手动设置依赖（因为我们没有使用Spring的自动注入）
        setDependencies(databaseTool, jdbcTemplate, namedJdbcTemplate);
        
        System.out.println("========== 数据库操作演示 ==========\n");
        
        try {
            // 1. 测试无参数查询
            System.out.println("===== 1. 查询所有数据 =====");
            String sql1 = "SELECT * FROM chatmemory LIMIT 10";
            String result1 = databaseTool.queryData(sql1);
            System.out.println(result1);
            
            // 2. 测试带参数查询
            System.out.println("\n===== 2. 按会话ID查询数据 =====");
            String sql2 = "SELECT * FROM chatmemory WHERE conversation_id = :conversationId";
            String params2 = "{\"conversationId\":\"abc-123\"}";
            String result2 = databaseTool.queryData(sql2, params2);
            System.out.println(result2);
            
            // 3. 测试数据插入
            System.out.println("\n===== 3. 插入数据 =====");
            String sql3 = "INSERT INTO chatmemory (conversation_id, message_order, message_type, content, message_json) " + 
                          "VALUES (:conversationId, :messageOrder, :messageType, :content, :messageJson)";
            String params3 = "{\"conversationId\":\"test-" + System.currentTimeMillis() + "\",\"messageOrder\":1," +
                          "\"messageType\":\"USER\",\"content\":\"测试消息\",\"messageJson\":\"{\\\"role\\\":\\\"user\\\",\\\"content\\\":\\\"测试消息\\\"}\"}";
            String result3 = databaseTool.insertData(sql3, params3);
            System.out.println(result3);
            
            // 4. 查询刚插入的数据
            System.out.println("\n===== 4. 查询刚才插入的数据 =====");
            String sql4 = "SELECT * FROM chatmemory ORDER BY id DESC LIMIT 1";
            String result4 = databaseTool.queryData(sql4);
            System.out.println(result4);
            
            // 5. 更新数据
            System.out.println("\n===== 5. 更新数据 =====");
            String sql5 = "UPDATE chatmemory SET content = :content WHERE id = (SELECT MAX(id) FROM chatmemory)";
            String params5 = "{\"content\":\"已更新的测试消息\"}";
            String result5 = databaseTool.updateData(sql5, params5);
            System.out.println(result5);
            
            // 6. 查询更新后的数据
            System.out.println("\n===== 6. 查询更新后的数据 =====");
            String sql6 = "SELECT * FROM chatmemory ORDER BY id DESC LIMIT 1";
            String result6 = databaseTool.queryData(sql6);
            System.out.println(result6);
            
            // 7. 逻辑删除数据
            System.out.println("\n===== 7. 逻辑删除数据 =====");
            String sql7 = "UPDATE chatmemory SET is_delete = :isDelete WHERE id = (SELECT MAX(id) FROM chatmemory)";
            String params7 = "{\"isDelete\":1}";
            String result7 = databaseTool.updateData(sql7, params7);
            System.out.println(result7);
            
        } catch (Exception e) {
            System.err.println("执行数据库操作时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n========== 演示完成 ==========");
    }
    
    /**
     * 手动设置依赖关系
     */
    private static void setDependencies(DatabaseOperationTool databaseTool, JdbcTemplate jdbcTemplate, 
                                      NamedParameterJdbcTemplate namedJdbcTemplate) {
        try {
            // 使用反射设置私有字段
            java.lang.reflect.Field jdbcField = DatabaseOperationTool.class.getDeclaredField("jdbcTemplate");
            jdbcField.setAccessible(true);
            jdbcField.set(databaseTool, jdbcTemplate);
            
            java.lang.reflect.Field namedJdbcField = DatabaseOperationTool.class.getDeclaredField("namedParameterJdbcTemplate");
            namedJdbcField.setAccessible(true);
            namedJdbcField.set(databaseTool, namedJdbcTemplate);
            
        } catch (Exception e) {
            System.err.println("设置依赖时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 