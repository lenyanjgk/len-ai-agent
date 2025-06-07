package com.lenyan.lenaiagent.agent;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 超级智能体，具备自主规划能力
 */
@Component
public class LenManus extends ToolCallAgent {

    public LenManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        
        // 基础配置
        this.setName("lenManus");
        this.setMaxSteps(20);
        
        // 提示词设置
        this.setSystemPrompt(
            "You are LenManus, an all-capable AI assistant, aimed at solving any task presented by the user. " +
            "You have various tools at your disposal that you can call upon to efficiently complete complex requests."
        );
        
        this.setNextStepPrompt(
            "Based on user needs, proactively select the most appropriate tool or combination of tools. " +
            "For complex tasks, you can break down the problem and use different tools step by step to solve it. " +
            "After using each tool, clearly explain the execution results and suggest the next steps. " +
            "If you want to stop the interaction at any point, use the `terminate` tool/function call."
        );
        
        // 初始化对话客户端
        this.setChatClient(
            ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build()
        );
    }
}
