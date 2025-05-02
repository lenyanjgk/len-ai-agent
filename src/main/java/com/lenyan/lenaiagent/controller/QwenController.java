package com.lenyan.lenaiagent.controller;

import com.lenyan.lenaiagent.domain.request.QwenMultiModalRequest;
import com.lenyan.lenaiagent.domain.request.QwenTextRequest;
import com.lenyan.lenaiagent.domain.response.QwenResponse;
import com.lenyan.lenaiagent.service.QwenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 通义千问控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qwen")
@Tag(name = "通义千问API", description = "阿里云通义千问大模型API")
public class QwenController {

    private final QwenService qwenService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 1. 文本对话
     */
    @PostMapping("/chat")
    @Operation(summary = "文本对话", description = "发送文本消息到通义千问进行对话")
    public QwenResponse chat(@RequestBody QwenTextRequest request) {
        log.info("接收到文本对话请求: {}", request.getContent());
        return qwenService.chat(request);
    }

    /**
     * 2. 流式文本对话
     */
    @PostMapping(value = "/stream-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式文本对话", description = "发送文本消息到通义千问进行流式对话")
    public SseEmitter streamChat(@RequestBody QwenTextRequest request) {
        log.info("接收到流式文本对话请求: {}", request.getContent());
        SseEmitter emitter = new SseEmitter(-1L); // 无超时

        executorService.execute(() -> {
            try {
                qwenService.streamChat(request)
                        .subscribe(
                                response -> {
                                    try {
                                        emitter.send(response);
                                    } catch (IOException e) {
                                        log.error("发送流式消息出错", e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                error -> {
                                    log.error("流式对话出错", error);
                                    emitter.completeWithError(error);
                                },
                                () -> {
                                    log.info("流式对话完成");
                                    emitter.complete();
                                });
            } catch (Exception e) {
                log.error("处理流式对话请求出错", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 3. 图像对话
     */
    @PostMapping("/image")
    @Operation(summary = "图像对话", description = "发送图像和文本到通义千问进行图像分析对话")
    public QwenResponse imageChat(@RequestBody QwenMultiModalRequest request) {
        log.info("接收到图像对话请求，图像数量: {}", request.getImageUrls().size());
        // 确保设置类型为IMAGE
        request.setType("IMAGE");
        // 确保使用视觉模型
        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel("qwen-vl-plus");
        }
        return qwenService.multiModalChat(request);
    }

    /**
     * 4. 视频对话
     */
    @PostMapping("/video")
    @Operation(summary = "视频对话", description = "发送视频URL或视频帧序列和文本到通义千问进行视频分析对话")
    public QwenResponse videoChat(@RequestBody QwenMultiModalRequest request) {
        if (request.getVideoUrls() != null && !request.getVideoUrls().isEmpty()) {
            if (request.getVideoUrls().size() == 1) {
                log.info("接收到视频对话请求，视频URL: {}", request.getVideoUrls().get(0));
            } else {
                log.info("接收到视频对话请求，帧数量: {}", request.getVideoUrls().size());
            }
        } else {
            log.warn("接收到视频对话请求，但未提供视频URL或帧序列");
        }

        // 确保设置类型为VIDEO
        request.setType("VIDEO");
        // 确保使用视频模型
        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel("qwen-vl-max-latest");
        }
        return qwenService.multiModalChat(request);
    }

    /**
     * 5. 音频对话
     */
    @PostMapping("/audio")
    @Operation(summary = "音频对话", description = "发送音频和文本到通义千问进行音频分析对话")
    public QwenResponse audioChat(@RequestBody QwenMultiModalRequest request) {
        // 确保类型为AUDIO
        request.setType("AUDIO");
        
        // 验证必要参数
        if (request.getAudioUrl() == null || request.getAudioUrl().isEmpty()) {
            return QwenResponse.builder()
                    .success(false)
                    .statusCode(400)
                    .message("错误：必须提供audioUrl参数")
                    .build();
        }
        
        log.info("接收到音频对话请求：audioUrl={}, textContent={}", 
                 request.getAudioUrl(), 
                 request.getTextContent());
        
        return qwenService.multiModalChat(request);
    }
}