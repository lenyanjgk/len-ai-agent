package com.lenyan.lenaiagent.app;

import com.lenyan.lenaiagent.advisor.MyLoggerAdvisor;
import com.lenyan.lenaiagent.advisor.ProhibitedWordAdvisor;
import com.lenyan.lenaiagent.chatmemory.MybatisPlusChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private static final String SYSTEM_PROMPT = "**恋爱大师·情感导航员**  \n" + "10年情感咨询经验，擅长亲密关系理论与沟通技巧。提供中立建议，保护隐私。通过情绪确认、需求拆解（3-5维度）、心理学理论（如非暴力沟通）解析问题，给出2种实操策略（如\"我句式\"对话模拟），引导关系边界建立。示例：\"遗忘纪念日可能涉及记忆模式/爱意表达方式差异，建议用'观察+感受'沟通\"。不评判道德、不做医疗建议，严守伦理规范。您的专属情感顾问，随时为您解惑。";
    private final ChatClient chatClient;

    public LoveApp(ChatModel dashscopeChatModel, MybatisPlusChatMemory chatMemory /*MySQLChatMemory chatMemory*/) {
//         String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//         ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

//        ChatMemory chatMemory = new InMemoryChatMemory();
//        ChatMemory chatMemory = new MySQLChatMemory(dataSource);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
                        // 记录日志
                        new MyLoggerAdvisor(),
                        // 违禁词检测 - 从文件读取违禁词
                        new ProhibitedWordAdvisor()
                        // 复读强化阅读能力
                        //new ReReadingAdvisor()
                ).build();
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


    /**
     * AI 恋爱报告功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call().entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public String doChatWithRagcloud(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用增强检索服务（云知识库服务）
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



}
