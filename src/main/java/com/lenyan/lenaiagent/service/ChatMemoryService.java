package com.lenyan.lenaiagent.service;

import com.lenyan.lenaiagent.domain.ChatMemory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 聊天记忆服务接口
 */
public interface ChatMemoryService extends IService<ChatMemory> {

    /**
     * 添加多条消息
     *
     * @param conversationId 会话ID
     * @param messages       消息列表
     */
    void addMessages(String conversationId, List<Message> messages);

    /**
     * 获取会话消息
     *
     * @param conversationId 会话ID
     * @param lastN          获取的消息数量，正数表示获取前N条，0或负数表示获取全部
     * @return 消息列表
     */
    List<Message> getMessages(String conversationId, int lastN);

    /**
     * 清除会话消息（逻辑删除）
     *
     * @param conversationId 会话ID
     */
    void clearMessages(String conversationId);

}
