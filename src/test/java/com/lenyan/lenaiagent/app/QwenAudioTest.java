package com.lenyan.lenaiagent.app;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.lenyan.lenaiagent.config.QwenConfig;
import com.lenyan.lenaiagent.domain.request.QwenMultiModalRequest;
import com.lenyan.lenaiagent.domain.response.QwenResponse;
import com.lenyan.lenaiagent.service.QwenService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;

/**
 * 通义千问多模态音频API测试类
 */
@SpringBootTest
public class QwenAudioTest {
    private static final Logger log = LoggerFactory.getLogger(QwenAudioTest.class);

    @Resource
    private QwenService qwenService;

    @Resource
    private QwenConfig qwenConfig;

    /**
     * 测试通义千问多模态API音频处理功能
     */
    @Test
    void testMultiModalAudioChat() {
        QwenMultiModalRequest request = QwenMultiModalRequest.builder()
                .conversationId("test-audio-" + System.currentTimeMillis())
                .textContent("请你分析一下这个音频讲述了什么内容")
                .audioUrl("https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3")
                .systemPrompt("You are a helpful assistant.")
                .type("AUDIO")
                .build();

        QwenResponse response = qwenService.multiModalChat(request);
        
        log.info("多模态音频API响应: {}", response);
        Assertions.assertNotNull(response);
        
        if (response.getSuccess() == null || !response.getSuccess()) {
            log.error("API调用失败: {}", response.getMessage());
        } else {
            log.info("音频分析结果: {}", response.getContent());
        }
    }

    /**
     * 直接使用DashScope SDK测试音频处理
     */
    @Test
    void testDirectDashScopeSDK() {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            
            // 使用示例音频URL
            String audioUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3";
            
            // 构建用户消息
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("audio", audioUrl),
                            Collections.singletonMap("text", "这段音频在说什么?")))
                    .build();
            
            // 构建参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model("qwen2-audio-instruct")
                    .message(userMessage)
                    .build();
            
            // 发送请求
            log.info("发送DashScope请求，音频URL: {}", audioUrl);
            MultiModalConversationResult result = conv.call(param);
            
            // 输出结果
            log.info("DashScope响应: {}", JsonUtils.toJson(result));
            Assertions.assertNotNull(result);
            
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("DashScope API调用失败: {}", e.getMessage());
            Assertions.fail("DashScope API调用失败: " + e.getMessage());
        }
    }

    /**
     * 测试推荐的阿里云OSS格式
     */
    @Test
    void testRecommendedFormat() {
        // 使用阿里云官方示例URL
        String recommendedUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3";
        
        try {
            MultiModalConversation conv = new MultiModalConversation();
            
            // 构建用户消息
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("audio", recommendedUrl),
                            Collections.singletonMap("text", "请分析这个音频内容")))
                    .build();
            
            // 构建参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model("qwen2-audio-instruct")
                    .message(userMessage)
                    .build();
            
            // 发送请求
            MultiModalConversationResult result = conv.call(param);
            
            // 验证结果
            Assertions.assertNotNull(result);
            log.info("使用推荐格式音频URL测试成功");
            
        } catch (Exception e) {
            log.error("使用推荐格式音频URL测试失败: {}", e.getMessage());
            Assertions.fail("API调用失败: " + e.getMessage());
        }
    }
} 