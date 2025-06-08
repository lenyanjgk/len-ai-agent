package com.lenyan.lenaiagent.agent;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 智慧答题助手，专注于辅助用户解答各类题目
 */
@Component
public class QuizAssistant extends ToolCallAgent {

    @Autowired
    public QuizAssistant(@Qualifier("allTools") ToolCallback[] allTools, 
                         @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel) {
        super(allTools);
        
        // 基础配置
        this.setName("quizAssistant");
        this.setMaxSteps(15);
        this.setDuplicateThreshold(3);
        
        // 提示词设置
        this.setSystemPrompt(
            "You are a professional assessment analysis assistant specialized in interpreting various test results and providing personalized analysis. " +
            "Your goal is to help users understand their assessment outcomes, analyze personal characteristics, " +
            "and offer targeted recommendations and improvement strategies. " +
            "You will adapt your analysis approach based on the assessment type (e.g., personality assessment, competency evaluation, emotional assessment) " +
            "to ensure the process is professional, comprehensive, and insightful. " +
            "Your responses should be well-structured, intuitive, and easy to understand. " +
            "When necessary, generate visually rich PDF reports with charts and images."
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