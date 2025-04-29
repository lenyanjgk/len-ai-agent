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
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MySQL实现的对话记忆
 * 将对话内容持久化到MySQL数据库
 */
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
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        add(conversationId, messages);
    }

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty() || conversationId == null) {
            return;
        }

        try {
            // 获取当前最大序号
            String maxOrderSql = "SELECT MAX(message_order) FROM chat_memory WHERE conversation_id = ? AND is_delete = 0";
            logSql(maxOrderSql, new Object[] { conversationId });

            Integer maxOrder = getMaxOrder(conversationId).orElse(0);
            int nextOrder = maxOrder + 1;

            // 使用批处理提高效率
            String insertSql = "INSERT INTO chat_memory (conversation_id, message_order, message_type, content, message_json, create_time, update_time, is_delete) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            log.info("SQL执行批处理: {}", insertSql);
            log.info("批处理参数示例 - conversationId: {}, 消息数量: {}", conversationId, messages.size());

            jdbcTemplate.batchUpdate(insertSql, messages, messages.size(), (ps, message) -> {
                try {
                    int order = nextOrder + messages.indexOf(message);
                    String messageJson = serializeMessage(message);
                    String content = extractContent(message);
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                    ps.setString(1, conversationId);
                    ps.setInt(2, order);
                    ps.setString(3, message.getMessageType().toString());
                    ps.setString(4, content);
                    ps.setString(5, messageJson);
                    ps.setTimestamp(6, now); // create_time
                    ps.setTimestamp(7, now); // update_time
                    ps.setBoolean(8, false); // is_delete = 0

                    // 记录每条消息的参数
                    if (log.isDebugEnabled()) {
                        log.debug("批处理参数 [{}]: conversationId={}, order={}, type={}, content={}",
                                messages.indexOf(message), conversationId, order, message.getMessageType(), content);
                    }
                } catch (Exception e) {
                    log.error("准备消息批处理时出错", e);
                    throw new SQLException("准备消息批处理失败", e);
                }
            });

            log.info("已添加 {} 条消息到会话 {} ,该消息为{}", messages.size(), conversationId, messages);
        } catch (Exception e) {
            log.error("向MySQL添加消息时出错", e);
            throw new RuntimeException("向聊天记忆添加消息失败", e);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        try {
            String sql;
            Object[] params;

            // 修改查询逻辑：lastN > 0 时获取前N条消息，而不是最后N条
            if (lastN > 0) {
                // 直接获取前lastN条消息，按message_order升序排序
                sql = "SELECT message_json, message_type, content FROM chat_memory " +
                        "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC LIMIT ?";
                params = new Object[] { conversationId, lastN };
            } else {
                // 获取全部消息
                sql = "SELECT message_json, message_type, content FROM chat_memory " +
                        "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC";
                params = new Object[] { conversationId };
            }
            // 记录SQL日志并执行查询
            List<Message> messages = executeMessageQuery(sql, params);
            log.info("已从会话 {} 中检索到 {} 条消息", conversationId, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("从MySQL检索消息时出错", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        try {
            // 将物理删除改为逻辑删除
            String sql = "UPDATE chat_memory SET is_delete = 1, update_time = ? WHERE conversation_id = ? AND is_delete = 0";
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            Object[] params = new Object[] { now, conversationId };
            logSql(sql, params);

            int count = jdbcTemplate.update(sql, params);
            log.info("已从会话 {} 中逻辑删除 {} 条消息", conversationId, count);
        } catch (Exception e) {
            log.error("从MySQL逻辑删除消息时出错", e);
        }
    }

    /**
     * 获取会话中最大的消息序号
     */
    private Optional<Integer> getMaxOrder(String conversationId) {
        try {
            String sql = "SELECT MAX(message_order) FROM chat_memory WHERE conversation_id = ? AND is_delete = 0";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class, conversationId));
        } catch (Exception e) {
            log.debug("会话 {} 中未找到现有消息", conversationId);
            return Optional.empty();
        }
    }

    /**
     * 从消息中提取内容文本
     */
    private String extractContent(Message message) {
        return message.getText();
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
     * 简化版本，直接通过消息类型创建对应的消息对象
     */
    private Message deserializeMessage(String messageJson, String messageType, String content) {
        try {
            switch (messageType) {
                case "USER":
                    return new UserMessage(content);
                case "ASSISTANT":
                    return new AssistantMessage(content);
                case "SYSTEM":
                    return new SystemMessage(content);
                default:
                    log.warn("未知的消息类型: {}", messageType);
                    // 改为使用AssistantMessage代替GenericMessage，简化处理
                    return new AssistantMessage("未知消息类型: " + content);
            }
        } catch (Exception e) {
            log.error("反序列化消息时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取特定会话的消息数量
     */
    public int getMessageCount(String conversationId) {
        try {
            String sql = "SELECT COUNT(*) FROM chat_memory WHERE conversation_id = ? AND is_delete = 0";
            logSql(sql, new Object[] { conversationId });

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("计算消息数量时出错", e);
            return 0;
        }
    }

    /**
     * 分页获取会话消息
     */
    public List<Message> getMessagesPaginated(String conversationId, int page, int pageSize) {
        int offset = page * pageSize;

        try {
            String sql = "SELECT message_json, message_type, content FROM chat_memory " +
                    "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC LIMIT ? OFFSET ?";
            Object[] params = new Object[] { conversationId, pageSize, offset };

            // 执行查询
            return executeMessageQuery(sql, params);
        } catch (Exception e) {
            log.error("检索分页消息时出错", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取最新的N条消息
     * 这个方法保留了原来get方法的行为 - 返回按message_order排序的最后N条消息
     *
     * @param conversationId 会话ID
     * @param lastN          获取的消息数量，如果为0或负数则获取全部消息
     * @return 消息列表，按message_order升序排序
     */
    public List<Message> getLatestMessages(String conversationId, int lastN) {
        try {
            String sql;
            Object[] params;

            if (lastN > 0) {
                // 获取总消息数量
                int totalMessages = getMessageCount(conversationId);

                if (totalMessages > lastN) {
                    // 如果总数大于lastN，获取最后lastN条
                    // 计算起始位置：总数 - lastN
                    int offset = totalMessages - lastN;
                    sql = "SELECT message_json, message_type, content FROM chat_memory " +
                            "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC LIMIT ? OFFSET ?";
                    params = new Object[] { conversationId, lastN, offset };
                } else {
                    // 如果总数不大于lastN，获取全部消息
                    sql = "SELECT message_json, message_type, content FROM chat_memory " +
                            "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC";
                    params = new Object[] { conversationId };
                }
            } else {
                // 获取全部消息
                sql = "SELECT message_json, message_type, content FROM chat_memory " +
                        "WHERE conversation_id = ? AND is_delete = 0 ORDER BY message_order DESC";
                params = new Object[] { conversationId };
            }

            // 执行查询
            List<Message> messages = executeMessageQuery(sql, params);
            log.info("已从会话 {} 中检索到最新的 {} 条消息", conversationId, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("从MySQL检索最新消息时出错", e);
            return new ArrayList<>();
        }
    }

    /**
     * 执行消息查询并返回结果列表
     *
     * @param sql    SQL查询语句
     * @param params 查询参数
     * @return 消息列表
     */
    private List<Message> executeMessageQuery(String sql, Object[] params) {
        // 记录SQL日志
        logSql(sql, params);

        // 执行查询
        List<Message> queryResults = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            try {
                String messageJson = rs.getString("message_json");
                String messageType = rs.getString("message_type");
                String content = rs.getString("content");
                return deserializeMessage(messageJson, messageType, content);
            } catch (Exception e) {
                log.error("反序列化消息时出错", e);
                return null;
            }
        });

        // 过滤掉null值
        List<Message> messages = new ArrayList<>();
        for (Message msg : queryResults) {
            if (msg != null) {
                messages.add(msg);
            }
        }

        return messages;
    }

    /**
     * 记录SQL语句和参数
     *
     * @param sql    SQL语句
     * @param params SQL参数数组
     */
    private void logSql(String sql, Object[] params) {
        log.info("SQL: {}", sql);
        log.info("参数: {}", Arrays.toString(params));
    }
}