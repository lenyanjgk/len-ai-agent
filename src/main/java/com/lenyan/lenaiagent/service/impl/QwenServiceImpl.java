package com.lenyan.lenaiagent.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.lenyan.lenaiagent.config.QwenConfig;
import com.lenyan.lenaiagent.domain.request.QwenMultiModalRequest;
import com.lenyan.lenaiagent.domain.request.QwenTextRequest;
import com.lenyan.lenaiagent.domain.response.QwenResponse;
import com.lenyan.lenaiagent.service.QwenService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 通义千问服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QwenServiceImpl implements QwenService {

    private final QwenConfig qwenConfig;

    @Override
    public QwenResponse chat(QwenTextRequest request) {
        try {
            Generation gen = new Generation();

            // 构建系统消息
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(request.getSystemPrompt())
                    .build();

            // 构建用户消息
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(request.getContent())
                    .build();

            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(systemMsg);

            // 添加历史消息
            if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                for (Map<String, String> historyMsg : request.getHistory()) {
                    String role = historyMsg.get("role");
                    String content = historyMsg.get("content");
                    messages.add(Message.builder()
                            .role(role)
                            .content(content)
                            .build());
                }
            }

            messages.add(userMsg);

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model(request.getModel() != null ? request.getModel() : qwenConfig.getTextModel())
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            // 发送请求
            GenerationResult result = gen.call(param);

            // 构建响应
            return buildResponse(result, request.getConversationId());
        } catch (Exception e) {
            log.error("调用通义千问文本API出错", e);
            return QwenResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .message("调用通义千问API失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Flowable<QwenResponse> streamChat(QwenTextRequest request) {
        try {
            Generation gen = new Generation();

            // 构建系统消息
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(request.getSystemPrompt())
                    .build();

            // 构建用户消息
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(request.getContent())
                    .build();

            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(systemMsg);

            // 添加历史消息
            if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                for (Map<String, String> historyMsg : request.getHistory()) {
                    String role = historyMsg.get("role");
                    String content = historyMsg.get("content");
                    messages.add(Message.builder()
                            .role(role)
                            .content(content)
                            .build());
                }
            }

            messages.add(userMsg);

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model(request.getModel() != null ? request.getModel() : qwenConfig.getTextModel())
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true)
                    .build();

            // 发送流式请求
            Flowable<GenerationResult> resultFlowable = gen.streamCall(param);

            // 转换为响应流
            return resultFlowable.map(result -> buildResponse(result, request.getConversationId()));
        } catch (Exception e) {
            log.error("调用通义千问流式文本API出错", e);
            return Flowable.error(e);
        }
    }

    @Override
    public QwenResponse multiModalChat(QwenMultiModalRequest request) {
        try {
            MultiModalConversation conv = new MultiModalConversation();

            // 构建多模态消息内容
            List<Map<String, Object>> contentList = new ArrayList<>();

            // 先验证类型字段和实际提供的内容是否匹配
            String actualType = determineActualType(request);
            if (!actualType.equals(request.getType())) {
                log.warn("请求类型({})与实际提供的内容类型({})不匹配", request.getType(), actualType);
                return createErrorResponse(String.format(
                    "请求类型(%s)与实际提供的内容类型(%s)不匹配。请确保类型字段与提供的内容一致。", 
                    request.getType(), actualType));
            }

            // 根据类型添加多模态内容
            if ("IMAGE".equalsIgnoreCase(request.getType())) {
                // 添加图像内容
                if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                    for (String imageUrl : request.getImageUrls()) {
                        if (isValidUrl(imageUrl)) {
                            contentList.add(Collections.singletonMap("image", imageUrl));
                            log.info("添加图像URL: {}", imageUrl);
                        } else {
                            log.warn("跳过无效图像URL: {}", imageUrl);
                            return createErrorResponse("图像URL无效或无法访问: " + imageUrl + 
                                "。请确保URL使用HTTPS协议并且是公开可访问的");
                        }
                    }
                } else {
                    log.warn("IMAGE类型请求但未提供图像URL");
                    return createErrorResponse("IMAGE类型请求必须提供至少一个图像URL");
                }
            } else if ("VIDEO".equalsIgnoreCase(request.getType())) {
                // 添加视频内容
                if (request.getVideoUrls() != null && !request.getVideoUrls().isEmpty()) {
                    // 检查是否是视频文件URL（通常以.mp4, .avi等结尾）
                    if (request.getVideoUrls().size() == 1 && isVideoFileUrl(request.getVideoUrls().get(0))) {
                        String videoUrl = request.getVideoUrls().get(0);
                        if (isValidUrl(videoUrl)) {
                            // 直接使用视频URL作为内容
                            contentList.add(Collections.singletonMap("video", videoUrl));
                            log.info("使用单个视频文件URL: {}", videoUrl);
                        } else {
                            log.warn("视频URL无效: {}", videoUrl);
                            return createErrorResponse("视频URL无效或无法访问: " + videoUrl + 
                                "。请确保URL使用HTTPS协议并且是公开可访问的");
                        }
                    } else {
                        // 验证所有帧URL
                        List<String> validVideoFrames = new ArrayList<>();
                        List<String> invalidVideoFrames = new ArrayList<>();
                        for (String frameUrl : request.getVideoUrls()) {
                            if (isValidUrl(frameUrl)) {
                                validVideoFrames.add(frameUrl);
                            } else {
                                invalidVideoFrames.add(frameUrl);
                                log.warn("跳过无效视频帧URL: {}", frameUrl);
                            }
                        }

                        if (validVideoFrames.isEmpty()) {
                            log.warn("没有有效的视频帧URL");
                            return createErrorResponse("所有视频帧URL均无效或无法访问: " + 
                                String.join(", ", invalidVideoFrames) + 
                                "。请确保URL使用HTTPS协议并且是公开可访问的");
                        }

                        // 使用一系列帧图像作为视频内容
                        contentList.add(Collections.singletonMap("video", validVideoFrames));
                        log.info("使用视频帧序列，有效帧数: {}", validVideoFrames.size());
                    }
                } else {
                    log.warn("VIDEO类型请求但未提供视频URL");
                    return createErrorResponse("VIDEO类型请求必须提供至少一个视频URL或帧序列");
                }
            } else if ("AUDIO".equalsIgnoreCase(request.getType())) {
                // 添加音频内容
                if (request.getAudioUrl() != null && !request.getAudioUrl().isEmpty()) {
                    String audioUrl = request.getAudioUrl();
                    
                    // 验证URL格式和协议
                    if (!isUrlFormatValid(audioUrl)) {
                        log.warn("音频URL格式无效: {}", audioUrl);
                        return createErrorResponse("音频URL格式无效: " + audioUrl);
                    }
                    
                    // 验证是否使用HTTPS协议
                    if (!isHttpsProtocol(audioUrl)) {
                        log.warn("音频URL不是HTTPS协议: {}", audioUrl);
                        return createErrorResponse("通义千问API要求使用HTTPS协议的URL。" +
                            "当前URL使用的是: " + getProtocol(audioUrl) + "。请将URL更改为HTTPS协议。");
                    }
                    
                    // 验证是否是音频文件
                    if (!isAudioFileUrl(audioUrl)) {
                        log.warn("URL不是支持的音频文件格式: {}", audioUrl);
                        return createErrorResponse("URL不是支持的音频文件格式: " + audioUrl + 
                            "。支持的格式包括: .mp3, .wav, .ogg, .m4a, .aac, .flac");
                    }

                    // 添加到内容列表
                    contentList.add(Collections.singletonMap("audio", audioUrl));
                    log.info("使用音频URL: {}", audioUrl);
                } else {
                    log.warn("AUDIO类型请求但未提供音频URL");
                    return createErrorResponse("AUDIO类型请求必须提供音频URL");
                }
            }

            // 添加文本内容
            if (request.getTextContent() != null && !request.getTextContent().isEmpty()) {
                contentList.add(Collections.singletonMap("text", request.getTextContent()));
            }

            if (contentList.isEmpty()) {
                log.warn("没有有效的多模态内容");
                return createErrorResponse("没有有效的多模态内容。请提供类型对应的必要内容。");
            }

            // 构建系统消息
            MultiModalMessage systemMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(Collections.singletonMap("text", request.getSystemPrompt())))
                    .build();

            // 构建用户消息
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(contentList)
                    .build();

            // 确定使用哪个模型
            String model;
            if (request.getModel() != null && !request.getModel().isEmpty()) {
                model = request.getModel();
            } else {
                if ("IMAGE".equalsIgnoreCase(request.getType())) {
                    model = qwenConfig.getVisionModel();
                } else if ("VIDEO".equalsIgnoreCase(request.getType())) {
                    model = qwenConfig.getVideoModel();
                } else if ("AUDIO".equalsIgnoreCase(request.getType())) {
                    model = qwenConfig.getAudioModel();
                } else {
                    model = qwenConfig.getTextModel();
                }
            }

            log.info("使用模型: {}, API密钥: {}", model, maskApiKey(qwenConfig.getApiKey()));

            // 构建参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(qwenConfig.getApiKey())
                    .model(model)
                    .message(systemMessage)
                    .message(userMessage)
                    .build();

            // 发送请求
            MultiModalConversationResult result = conv.call(param);

            // 构建响应
            return buildMultiModalResponse(result, request.getConversationId());
        } catch (Exception e) {
            log.error("调用通义千问多模态API出错", e);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Failed to download multimodal content")) {
                return QwenResponse.builder()
                        .success(false)
                        .statusCode(400)
                        .message("无法下载多模态内容。请确保提供的URL: 1) 使用HTTPS协议; 2) 指向公开可访问的资源; " +
                                 "3) 使用支持的文件格式; 4) 不使用IP地址或非标准端口。")
                        .build();
            }
            return QwenResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .message("调用通义千问多模态API失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Flowable<QwenResponse> streamMultiModalChat(QwenMultiModalRequest request) {
        // TODO: 实现多模态流式对话
        // 注意：当前DashScope SDK可能不支持多模态的流式输出
        // 这里先返回一个错误
        log.warn("通义千问多模态流式API目前不支持");
        return Flowable.error(new UnsupportedOperationException("通义千问多模态流式API目前不支持"));
    }

    /**
     * 构建文本响应
     */
    private QwenResponse buildResponse(GenerationResult result, String conversationId) {
        QwenResponse response = new QwenResponse();

        // 如果会话ID为空，生成一个新的
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        response.setConversationId(conversationId);
        response.setRequestId(result.getRequestId());
        response.setSuccess(true);
        response.setStatusCode(200);

        // 设置内容
        if (result.getOutput() != null) {
            if (result.getOutput().getChoices() != null && !result.getOutput().getChoices().isEmpty()) {
                response.setContent(result.getOutput().getChoices().get(0).getMessage().getContent());
                response.setFinishReason(result.getOutput().getChoices().get(0).getFinishReason());
            } else if (result.getOutput().getText() != null) {
                response.setContent(result.getOutput().getText());
                response.setFinishReason(result.getOutput().getFinishReason());
            }
        }

        // 设置用量信息
        if (result.getUsage() != null) {
            QwenResponse.Usage usage = new QwenResponse.Usage();
            usage.setInputTokens(result.getUsage().getInputTokens());
            usage.setOutputTokens(result.getUsage().getOutputTokens());
            usage.setTotalTokens(result.getUsage().getTotalTokens());
            response.setUsage(usage);
        }

        return response;
    }

    /**
     * 构建多模态响应
     */
    private QwenResponse buildMultiModalResponse(MultiModalConversationResult result, String conversationId) {
        QwenResponse response = new QwenResponse();

        // 如果会话ID为空，生成一个新的
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        response.setConversationId(conversationId);
        response.setRequestId(result.getRequestId());
        response.setSuccess(true);
        response.setStatusCode(200);

        // 设置内容
        if (result.getOutput() != null && result.getOutput().getChoices() != null
                && !result.getOutput().getChoices().isEmpty()) {
            String content = null;

            // 获取内容 - 处理不同类型的返回结果
            Object contentObj = result.getOutput().getChoices().get(0).getMessage().getContent();
            if (contentObj instanceof String) {
                content = (String) contentObj;
            } else if (contentObj instanceof List<?>) {
                List<?> contentList = (List<?>) contentObj;
                if (!contentList.isEmpty()) {
                    if (contentList.get(0) instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> contentMap = (Map<String, Object>) contentList.get(0);
                        if (contentMap.containsKey("text")) {
                            content = contentMap.get("text").toString();
                        }
                    }
                }
            }

            response.setContent(content);
            response.setFinishReason(result.getOutput().getChoices().get(0).getFinishReason());
        }

        // 设置用量信息
        if (result.getUsage() != null) {
            QwenResponse.Usage usage = new QwenResponse.Usage();
            usage.setInputTokens(result.getUsage().getInputTokens());
            usage.setOutputTokens(result.getUsage().getOutputTokens());

            // 多模态特有的Token计算
            if (result.getUsage().getImageTokens() != null) {
                usage.setImageTokens(result.getUsage().getImageTokens());
            }
            if (result.getUsage().getVideoTokens() != null) {
                usage.setVideoTokens(result.getUsage().getVideoTokens());
            }
            if (result.getUsage().getAudioTokens() != null) {
                usage.setAudioTokens(result.getUsage().getAudioTokens());
            }

            response.setUsage(usage);
        }

        return response;
    }

    /**
     * 创建错误响应
     */
    private QwenResponse createErrorResponse(String errorMessage) {
        return QwenResponse.builder()
                .success(false)
                .statusCode(400)
                .message(errorMessage)
                .build();
    }

    /**
     * 判断URL是否是视频文件URL
     */
    private boolean isVideoFileUrl(String url) {
        if (url == null)
            return false;
        String lowercaseUrl = url.toLowerCase();
        return lowercaseUrl.endsWith(".mp4") ||
                lowercaseUrl.endsWith(".avi") ||
                lowercaseUrl.endsWith(".mov") ||
                lowercaseUrl.endsWith(".wmv") ||
                lowercaseUrl.endsWith(".flv") ||
                lowercaseUrl.endsWith(".mkv") ||
                lowercaseUrl.endsWith(".webm");
    }

    /**
     * 判断URL是否是音频文件URL
     */
    private boolean isAudioFileUrl(String url) {
        if (url == null)
            return false;
        String lowercaseUrl = url.toLowerCase();
        return lowercaseUrl.endsWith(".mp3") ||
                lowercaseUrl.endsWith(".wav") ||
                lowercaseUrl.endsWith(".ogg") ||
                lowercaseUrl.endsWith(".m4a") ||
                lowercaseUrl.endsWith(".aac") ||
                lowercaseUrl.endsWith(".flac");
    }

    /**
     * 验证URL是否有效
     * 检查URL是否使用https协议
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();

            // 通义千问API要求使用HTTPS协议
            if (!"https".equalsIgnoreCase(protocol)) {
                log.warn("URL协议不是HTTPS: {}", url);
                return false;
            }

            // 检查是否使用IP地址
            if (isIpAddress(host)) {
                log.warn("URL使用IP地址，可能不被支持: {}", url);
                // 我们仍然返回true，因为有些公共API可能使用IP
            }

            // 检查是否使用非标准端口
            if (port != -1 && port != 443) {
                log.warn("URL使用非标准HTTPS端口: {}:{}", host, port);
                // 我们仍然返回true，因为有些公共API可能使用自定义端口
            }

            return true;
        } catch (Exception e) {
            log.warn("URL格式无效: {}", url);
            return false;
        }
    }

    /**
     * 判断主机名是否为IP地址
     */
    private boolean isIpAddress(String host) {
        if (host == null) return false;
        // 简单检查是否为IPv4地址
        String ipv4Pattern = "^(\\d{1,3}\\.){3}\\d{1,3}$";
        return host.matches(ipv4Pattern);
    }

    /**
     * 根据请求内容确定实际类型
     */
    private String determineActualType(QwenMultiModalRequest request) {
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            return "IMAGE";
        } else if (request.getVideoUrls() != null && !request.getVideoUrls().isEmpty()) {
            return "VIDEO";
        } else if (request.getAudioUrl() != null && !request.getAudioUrl().isEmpty()) {
            return "AUDIO";
        } else {
            return "TEXT";
        }
    }

    /**
     * 检查URL格式是否有效
     */
    private boolean isUrlFormatValid(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            log.warn("URL格式无效: {}", url);
            return false;
        }
    }

    /**
     * 检查是否使用HTTPS协议
     */
    private boolean isHttpsProtocol(String url) {
        if (url == null) return false;
        try {
            URL parsedUrl = new URL(url);
            return "https".equalsIgnoreCase(parsedUrl.getProtocol());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取URL的协议
     */
    private String getProtocol(String url) {
        if (url == null) return "unknown";
        try {
            URL parsedUrl = new URL(url);
            return parsedUrl.getProtocol();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 掩盖API密钥用于日志
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        // 只显示前3位和后3位
        return apiKey.substring(0, 3) + "..." + apiKey.substring(apiKey.length() - 3);
    }
}