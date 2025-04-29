package com.lenyan.lenaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lenyan.lenaiagent.domain.ChatMemory;
import com.lenyan.lenaiagent.mapper.ChatMemoryMapper;
import com.lenyan.lenaiagent.service.ChatMemoryService;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天记忆服务实现类
 */
@Slf4j
@Service
public class ChatMemoryServiceImpl extends ServiceImpl<ChatMemoryMapper, ChatMemory>
        implements ChatMemoryService {

    private final JSONConfig jsonConfig;

    public ChatMemoryServiceImpl() {
        this.jsonConfig = new JSONConfig().setIgnoreNullValue(true);
        log.info("初始化Mybatis-Plus聊天记忆服务");
    }

    @Override
    @Transactional
    public void addMessages(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty() || conversationId == null) {
            return;
        }

        // 获取当前最大序号
        Integer maxOrder = baseMapper.getMaxOrder(conversationId);
        int nextOrder = (maxOrder != null ? maxOrder : 0) + 1;

        // 将SpringAI消息转换为实体
        List<ChatMemory> entities = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            int order = nextOrder + i;

            ChatMemory entity = ChatMemory.builder()
                    .conversationId(conversationId)
                    .messageOrder(order)
                    .messageType(message.getMessageType().toString())
                    .content(message.getText())
                    .messageJson(serializeMessage(message))
                    .createTime(new Date())
                    .updateTime(new Date())
                    .isDelete(false)
                    .build();

            entities.add(entity);
        }

        // 批量保存
        saveBatch(entities);
        log.info("已添加 {} 条消息到会话 {}", messages.size(), conversationId);
    }

    @Override
    public List<Message> getMessages(String conversationId, int lastN) {
        List<ChatMemory> entities;

        if (lastN > 0) {
            // 获取最近的N条消息
            entities = baseMapper.getLatestMessages(conversationId, lastN);
        } else {
            // 获取全部消息
            LambdaQueryWrapper<ChatMemory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMemory::getConversationId, conversationId)
                    .eq(ChatMemory::getIsDelete, false)
                    .orderByDesc(ChatMemory::getMessageOrder);
            entities = list(wrapper);
        }

        // 将实体转换为SpringAI消息
        List<Message> messages = convertToMessages(entities);
        log.info("已从会话 {} 中检索到 {} 条消息", conversationId, messages.size());
        return messages;
    }

    @Override
    @Transactional
    public void clearMessages(String conversationId) {
        // 逻辑删除所有会话消息
        int count = baseMapper.logicalDeleteByConversationId(conversationId);
        log.info("已从会话 {} 中逻辑删除 {} 条消息", conversationId, count);
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
     * 将实体列表转换为SpringAI消息列表
     */
    private List<Message> convertToMessages(List<ChatMemory> entities) {
        return entities.stream()
                .map(this::convertToMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将单个实体转换为SpringAI消息
     */
    private Message convertToMessage(ChatMemory entity) {
        String messageType = entity.getMessageType();
        String content = entity.getContent();

        // 基于消息类型创建相应的消息实例
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
}
