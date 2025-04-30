package com.lenyan.lenaiagent.chatmemory;

import com.lenyan.lenaiagent.service.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于Mybatis-Plus实现的对话记忆
 * 使用ChatMemoryService进行数据库操作
 */
@Component
@Slf4j
public class MybatisPlusChatMemory implements ChatMemory {

    private final ChatMemoryService chatMemoryService;

    public MybatisPlusChatMemory(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
        log.info("初始化Mybatis-Plus对话记忆");
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        chatMemoryService.addMessages(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return chatMemoryService.getMessages(conversationId, lastN);
    }

    @Override
    public void clear(String conversationId) {
        chatMemoryService.clearMessages(conversationId);
    }
}
