package com.lenyan.lenaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;

/**
 * Spring AI 框架调用 AI 大模型（Ollama）
 */
// @Component
public class OllamaAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel ollamaChatModel;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("ollama调用: " +
                ollamaChatModel.call(new Prompt("你好，我是lenyan"))
                        .getResult()
                        .getOutput()
                        .getText());
    }
}
