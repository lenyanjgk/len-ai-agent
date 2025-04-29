package com.lenyan.lenaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;

//@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    // @Resource
    // private ChatClient chatClient;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("springai调用:" + dashscopeChatModel.call(new Prompt("你好，我是lenyan")).getResult().getOutput().getText());

        // chatClient.prompt("你好，我是lenyan").call();
    }
}
