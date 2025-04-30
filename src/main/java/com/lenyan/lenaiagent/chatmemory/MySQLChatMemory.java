package com.lenyan.lenaiagent.chatmemory;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MySQL实现的对话记忆
 * 将对话内容持久化到MySQL数据库
 */
@Component
@Slf4j
public class MySQLChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;
    private final JSONConfig jsonConfig;

    public MySQLChatMemory(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jsonConfig = new JSONConfig().setIgnoreNullValue(true);
        log.info("初始化MySQL对话记忆");
    }

    @Override
    @Transactional
    public void add(String conversationId, Message message) {
        if (message != null && conversationId != null) {
            List<Message> messages = Collections.singletonList(message);
            add(conversationId, messages);
        }
    }

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty() || conversationId == null) {
            return;
        }

        // 获取当前最大序号
        Integer maxOrder = getMaxOrder(conversationId).orElse(0);
        int nextOrder = maxOrder + 1;

        // 使用批处理提高效率
        String insertSql = "INSERT INTO chatmemory (conversation_id, message_order, message_type, content, message_json, create_time, update_time, is_delete) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        log.info("添加消息到会话 {}, 消息数量: {}", conversationId, messages.size());

        jdbcTemplate.batchUpdate(insertSql, messages, messages.size(), (ps, message) -> {
            int order = nextOrder + messages.indexOf(message);
            String messageJson = serializeMessage(message);
            String content = message.getText();
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            ps.setString(1, conversationId);
            ps.setInt(2, order);
            ps.setString(3, message.getMessageType().toString());
            ps.setString(4, content);
            ps.setString(5, messageJson);
            ps.setTimestamp(6, now); // create_time
            ps.setTimestamp(7, now); // update_time
            ps.setBoolean(8, false); // is_delete = 0
        });
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String sql;
        Object[] params;

        // 修改查询逻辑：lastN > 0 时获取前N条消息，而不是最后N条
        if (lastN > 0) {
            sql = "SELECT message_json, message_type, content FROM chatmemory " +
                    "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC LIMIT ?";
            params = new Object[] { conversationId, lastN };
        } else {
            sql = "SELECT message_json, message_type, content FROM chatmemory " +
                    "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC";
            params = new Object[] { conversationId };
        }

        List<Message> messages = executeMessageQuery(sql, params);
        log.info("从会话 {} 中检索到 {} 条消息", conversationId, messages.size());
        return messages;
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        // 将物理删除改为逻辑删除
        String sql = "UPDATE chatmemory SET is_delete = 1, update_time = ? WHERE conversation_id = ? AND is_delete = 0";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Object[] params = new Object[] { now, conversationId };

        int count = jdbcTemplate.update(sql, params);
        log.info("从会话 {} 中逻辑删除 {} 条消息", conversationId, count);
    }

    /**
     * 获取会话中最大的消息序号
     */
    private Optional<Integer> getMaxOrder(String conversationId) {
        String sql = "SELECT MAX(message_order) FROM chatmemory WHERE conversation_id = ? AND is_delete = 0";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, conversationId);
        return Optional.ofNullable(result);
    }

    /**
     * 将消息序列化为JSON字符串
     */
    private String serializeMessage(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", message.getMessageType().toString());
        map.put("text", message.getText());

        // 添加消息类名，便于反序列化
        if (message instanceof UserMessage) {
            map.put("messageClass", "UserMessage");
        } else if (message instanceof AssistantMessage) {
            map.put("messageClass", "AssistantMessage");
        } else if (message instanceof SystemMessage) {
            map.put("messageClass", "SystemMessage");
        } else {
            map.put("messageClass", "OtherMessage");
        }

        return JSONUtil.toJsonStr(map, jsonConfig);
    }

    /**
     * 从JSON字符串反序列化消息
     */
    private Message deserializeMessage(String messageJson, String messageType, String content) {
        switch (messageType) {
            case "USER":
                return new UserMessage(content);
            case "ASSISTANT":
                return new AssistantMessage(content);
            case "SYSTEM":
                return new SystemMessage(content);
            default:
                log.warn("未知的消息类型: {}", messageType);
                return new AssistantMessage("未知消息类型: " + content);
        }
    }

    /**
     * 执行消息查询并返回结果列表
     */
    private List<Message> executeMessageQuery(String sql, Object[] params) {
        log.info("SQL: {}, 参数: {}", sql, Arrays.toString(params));

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String messageJson = rs.getString("message_json");
            String messageType = rs.getString("message_type");
            String content = rs.getString("content");
            return deserializeMessage(messageJson, messageType, content);
        }).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}