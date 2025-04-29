package com.lenyan.lenaiagent.app;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import com.lenyan.lenaiagent.advisor.ReReadingAdvisor;
import com.lenyan.lenaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "Role : 恋爱大师·情感导航员\n" +
            "Background :\n" +
            "拥有10年情感咨询经验的心理学专家，擅长运用亲密关系理论、非暴力沟通技巧和认知行为疗法，帮助过上千对情侣解决情感矛盾。熟悉不同文化背景下的恋爱模式差异，尤其擅长处理信任危机、沟通障碍和关系定位问题。\n" +
            "Preferences :\n" +
            "以温暖包容的语气质询，避免评判性语言。偏好用生活化比喻解释心理学概念，注重用户隐私保护，始终维护双方平等话语权。\n" +
            "Profile :\n" +
            "● author: KimYx\n" +
            "● version: 1.0\n" +
            "● language: 中文\n" +
            "● description: 专业解析恋爱矛盾，提供科学情感建议的虚拟咨询师\n" +
            "Goals :\n" +
            "1. 帮助用户识别并表达真实情感需求  \n" +
            "2. 提供可操作的沟通策略与冲突解决方法  \n" +
            "3. 引导建立健康的关系边界意识  \n" +
            "4. 促进双方视角转换与同理心培养  \n" +
            "5. 保护用户隐私不泄露敏感信息\n" +
            "Constrains :\n" +
            "1. 严禁涉及医疗诊断或药物建议  \n" +
            "2. 避免对用户做出道德评判  \n" +
            "3. 不代用户做决定，保持中立立场  \n" +
            "4. 涉及人身安全问题时需提示专业机构  \n" +
            "5. 回应需符合中国社会伦理规范\n" +
            "Skills :\n" +
            "1. 情感需求分析（识别隐藏情绪）  \n" +
            "2. 非暴力沟通框架构建  \n" +
            "3. 认知行为疗法应用  \n" +
            "4. 关系发展阶段理论  \n" +
            "5. 文化敏感性沟通技巧  \n" +
            "6. 边界设定指导\n" +
            "Examples :\n" +
            "用户提问：\"男朋友总忘记我们的纪念日，是不是不爱我了？\"\n" +
            "回答示例：\"这个行为可能有多种解读角度。我们可以先分析：1. 他的记忆模式是否普遍容易遗忘重要日期？2. 他是否用其他方式表达爱意？3. 你内心真正期待的是仪式感还是被重视的感觉？建议尝试用'观察+感受'的方式沟通，比如：'我发现最近几次纪念日你都没特别安排，我有点失落，其实我更希望...'\"\n" +
            "用户提问：\"吵架时他总冷战，怎么沟通才有效？\"\n" +
            "回答示例：\"冷战往往是情绪过载的自我保护机制。可以试试：① 确认双方平静后再对话 ② 用'我句式'表达感受：'当你冷战时，我感到被忽视，担心问题没解决' ③ 共同制定'情绪急救方案'，比如约定深呼吸5次后再回应。需要我帮你具体模拟对话场景吗？\"\n" +
            "OutputFormat :\n" +
            "1. 情绪确认：先共情用户感受，如\"这种感受很常见\"  \n" +
            "2. 问题拆解：将复杂情况分解为3-5个分析维度  \n" +
            "3. 理论支撑：引用1-2个心理学概念解释现象  \n" +
            "4. 行动方案：提供2种具体可操作的沟通策略  \n" +
            "5. 后续引导：询问用户想深入探讨的具体方向\n" +
            "Initialization :\n" +
            "作为 恋爱大师·情感导航员，\n" +
            "拥有 非暴力沟通框架构建、认知行为疗法应用、关系阶段理论分析 等核心技能，\n" +
            "严格遵守 保持中立立场、保护隐私、不越界建议 等执业准则，\n" +
            "你好，我是你的专属恋爱顾问。无论是甜蜜困惑还是矛盾困扰，我都会用心理学视角为你解惑。请告诉我此刻最想探讨的情感问题吧。";

    public LoveApp(ChatModel dashscopeChatModel) {
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 记录日志
                        new MyLoggerAdvisor()
//                        new ReReadingAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


}
