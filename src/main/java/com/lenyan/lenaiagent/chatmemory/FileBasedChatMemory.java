package com.lenyan.lenaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件持久化的对话记忆
 */
@Slf4j
public class FileBasedChatMemory implements ChatMemory {

    private final String baseDir;
    private static final Kryo kryo;

    static {
        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String dir) {
        this.baseDir = dir;
        new File(dir).mkdirs();
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        var existingMessages = getOrCreateConversation(conversationId);
        existingMessages.addAll(messages);
        saveConversation(conversationId, existingMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        var allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        var file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    private List<Message> getOrCreateConversation(String conversationId) {
        var file = getConversationFile(conversationId);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (var input = new Input(new FileInputStream(file))) {
            return kryo.readObject(input, ArrayList.class);
        } catch (Exception e) {
            log.error("读取对话记录失败: {}", conversationId, e);
            return new ArrayList<>();
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        var file = getConversationFile(conversationId);
        try (var output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (Exception e) {
            log.error("保存对话记录失败: {}", conversationId, e);
        }
    }

    private File getConversationFile(String conversationId) {
        return new File(baseDir, conversationId + ".kryo");
    }
}
