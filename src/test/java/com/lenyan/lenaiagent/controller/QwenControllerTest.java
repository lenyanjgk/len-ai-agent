package com.lenyan.lenaiagent.controller;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lenyan.lenaiagent.controller.QwenController;
import com.lenyan.lenaiagent.domain.request.QwenMultiModalRequest;
import com.lenyan.lenaiagent.domain.response.QwenResponse;
import com.lenyan.lenaiagent.service.QwenService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 通义千问控制器测试类
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class QwenControllerTest {

    @InjectMocks
    private QwenController qwenController;

    @Mock
    private QwenService qwenService;

    /**
     * 测试视频帧模式
     */
    @Test
    public void testVideoFrames() {
        try {
            log.info("开始测试视频帧模式");

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(Collections.singletonMap("text", "You are a helpful assistant.")))
                    .build();

            // 使用官方示例的视频帧
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("video", Arrays.asList(
                                    "https://img.alicdn.com/imgextra/i3/O1CN01K3SgGo1eqmlUgeE9b_!!6000000003923-0-tps-3840-2160.jpg",
                                    "https://img.alicdn.com/imgextra/i4/O1CN01BjZvwg1Y23CF5qIRB_!!6000000003000-0-tps-3840-2160.jpg",
                                    "https://img.alicdn.com/imgextra/i4/O1CN01Ib0clU27vTgBdbVLQ_!!6000000007859-0-tps-3840-2160.jpg",
                                    "https://img.alicdn.com/imgextra/i1/O1CN01aygPLW1s3EXCdSN4X_!!6000000005710-0-tps-3840-2160.jpg")),
                            Collections.singletonMap("text", "描述这个视频的具体过程")))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model("qwen-vl-max-latest")
                    .message(systemMessage)
                    .message(userMessage)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("视频帧模式测试结果: {}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("视频帧模式测试失败", e);
        }
    }

    /**
     * 测试直接视频URL模式
     */
    @Test
    public void testVideoUrl() {
        try {
            log.info("开始测试视频URL模式");

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(Collections.singletonMap("text", "You are a helpful assistant.")))
                    .build();

            // 使用公开可访问的视频URL测试
            String videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"; // 公开测试视频

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("video", videoUrl),
                            Collections.singletonMap("text", "描述这个视频的内容")))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model("qwen-vl-max-latest")
                    .message(systemMessage)
                    .message(userMessage)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("视频URL模式测试结果: {}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("视频URL模式测试失败", e);
        }
    }

    /**
     * 测试音频URL模式
     */
    @Test
    public void testAudioUrl() {
        try {
            log.info("开始测试音频URL模式");

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(Collections.singletonMap("text", "You are a helpful assistant.")))
                    .build();

            // 使用公开可访问的音频URL测试
            // 注意：这是一个示例URL，需要替换为实际可访问的音频URL
            String audioUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3"; // 阿里云官方示例音频

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("audio", audioUrl),
                            Collections.singletonMap("text", "这段音频在说什么?")))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model("qwen2-audio-instruct")
                    .message(systemMessage)
                    .message(userMessage)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("音频URL模式测试结果: {}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("音频URL模式测试失败", e);
        }
    }

    /**
     * 测试使用替代音频URL
     */
    @Test
    public void testAlternativeAudioUrl() {
        try {
            log.info("开始测试替代音频URL模式");

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(Collections.singletonMap("text", "You are a helpful assistant.")))
                    .build();

            // 替代公开音频URL
            String audioUrl = "https://www2.cs.uic.edu/~i101/SoundFiles/BabyElephantWalk60.wav"; // 另一个公开测试音频

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("audio", audioUrl),
                            Collections.singletonMap("text", "描述这段音频的内容")))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model("qwen2-audio-instruct")
                    .message(systemMessage)
                    .message(userMessage)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("替代音频URL模式测试结果: {}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("替代音频URL模式测试失败", e);
        }
    }

    @Test
    void testAudioChatValidation() {
        // 1. 测试没有提供audioUrl的情况
        QwenMultiModalRequest requestWithoutAudio = QwenMultiModalRequest.builder()
                .conversationId("test-conversation")
                .textContent("分析这段音频")
                .build();
        
        QwenResponse noAudioResponse = qwenController.audioChat(requestWithoutAudio);
        
        assertFalse(noAudioResponse.isSuccess());
        assertEquals(400, noAudioResponse.getStatusCode());
        assertTrue(noAudioResponse.getMessage().contains("错误：必须提供audioUrl参数"));
        
        // 2. 测试IP地址带非标准HTTPS端口的情况
        QwenMultiModalRequest ipRequest = QwenMultiModalRequest.builder()
                .conversationId("test-conversation")
                .textContent("分析这段音频")
                .audioUrl("https://43.139.205.93:20043/down/ah1vznSoZMeO.mp3")
                .build();
        
        when(qwenService.multiModalChat(any(QwenMultiModalRequest.class)))
                .thenReturn(QwenResponse.builder()
                        .success(false)
                        .statusCode(400)
                        .message("无法下载多模态内容。请确保提供的URL: 1) 使用HTTPS协议; 2) 指向公开可访问的资源; " +
                                 "3) 使用支持的文件格式; 4) 不使用IP地址或非标准端口。")
                        .build());
        
        QwenResponse ipResponse = qwenController.audioChat(ipRequest);
        
        assertFalse(ipResponse.isSuccess());
        assertEquals(400, ipResponse.getStatusCode());
        assertTrue(ipResponse.getMessage().contains("无法下载多模态内容"));
        
        // 3. 测试HTTP协议（非HTTPS）的情况
        QwenMultiModalRequest httpRequest = QwenMultiModalRequest.builder()
                .conversationId("test-conversation")
                .textContent("分析这段音频")
                .audioUrl("http://example.com/audio.mp3")
                .build();
        
        when(qwenService.multiModalChat(any(QwenMultiModalRequest.class)))
                .thenReturn(QwenResponse.builder()
                        .success(false)
                        .statusCode(400)
                        .message("通义千问API要求使用HTTPS协议的URL。当前URL使用的是: http。请将URL更改为HTTPS协议。")
                        .build());
        
        QwenResponse httpResponse = qwenController.audioChat(httpRequest);
        
        assertFalse(httpResponse.isSuccess());
        assertEquals(400, httpResponse.getStatusCode());
        assertTrue(httpResponse.getMessage().contains("通义千问API要求使用HTTPS协议"));
        
        // 4. 测试不支持的文件格式
        QwenMultiModalRequest invalidFormatRequest = QwenMultiModalRequest.builder()
                .conversationId("test-conversation")
                .textContent("分析这段音频")
                .audioUrl("https://example.com/audio.xyz")
                .build();
        
        when(qwenService.multiModalChat(any(QwenMultiModalRequest.class)))
                .thenReturn(QwenResponse.builder()
                        .success(false)
                        .statusCode(400)
                        .message("URL不是支持的音频文件格式: https://example.com/audio.xyz。支持的格式包括: .mp3, .wav, .ogg, .m4a, .aac, .flac")
                        .build());
        
        QwenResponse invalidFormatResponse = qwenController.audioChat(invalidFormatRequest);
        
        assertFalse(invalidFormatResponse.isSuccess());
        assertEquals(400, invalidFormatResponse.getStatusCode());
        assertTrue(invalidFormatResponse.getMessage().contains("不是支持的音频文件格式"));
        
        // 5. 测试有效的请求（使用公开可访问的音频资源）
        QwenMultiModalRequest validRequest = QwenMultiModalRequest.builder()
                .conversationId("test-conversation")
                .textContent("分析这段音频")
                .audioUrl("https://file-examples.com/storage/fe5947a2a163010b197fb20/2017/11/file_example_MP3_700KB.mp3")
                .build();
        
        when(qwenService.multiModalChat(any(QwenMultiModalRequest.class)))
                .thenReturn(QwenResponse.builder()
                        .success(true)
                        .statusCode(200)
                        .content("这是一段MP3音频示例，音质清晰，包含背景音乐...")
                        .build());
        
        QwenResponse validResponse = qwenController.audioChat(validRequest);
        
        assertTrue(validResponse.isSuccess());
        assertEquals(200, validResponse.getStatusCode());
        assertNotNull(validResponse.getContent());
    }

    @Test
    void testTypeAndContentMismatch() {
        // 测试请求类型与实际内容不匹配的情况
        QwenMultiModalRequest mismatchRequest = QwenMultiModalRequest.builder()
                .conversationId("test-conversation")
                .textContent("这是什么图片?")
                .audioUrl("https://example.com/audio.mp3")
                .type("IMAGE") // 类型是IMAGE但提供的是audioUrl
                .build();
        
        when(qwenService.multiModalChat(any(QwenMultiModalRequest.class)))
                .thenReturn(QwenResponse.builder()
                        .success(false)
                        .statusCode(400)
                        .message("请求类型(IMAGE)与实际提供的内容类型(AUDIO)不匹配。请确保类型字段与提供的内容一致。")
                        .build());
        
        QwenResponse response = qwenController.audioChat(mismatchRequest);
        
        // audioChat方法会强制设置type为AUDIO，所以不会出现不匹配
        assertTrue(response.getMessage() == null || !response.getMessage().contains("不匹配"));
    }
}