package com.lenyan.lenaiagent.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通义千问多模态请求类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通义千问多模态请求")
public class QwenMultiModalRequest {

    @Schema(description = "会话ID", example = "conversation-12345")
    private String conversationId;

    @Schema(description = "文本内容", example = "这张图片是什么?")
    private String textContent;

    @Schema(description = "图像URL列表", example = "['https://example.com/image.jpg']")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Schema(description = "视频URL或图像帧列表", example = "['https://example.com/frame1.jpg', 'https://example.com/frame2.jpg']")
    @Builder.Default
    private List<String> videoUrls = new ArrayList<>();

    @Schema(description = "音频URL", example = "https://example.com/audio.mp3")
    private String audioUrl;

    @Schema(description = "系统提示词", example = "You are a helpful assistant.")
    @Builder.Default
    private String systemPrompt = "You are a helpful assistant.";

    @Schema(description = "历史消息列表")
    @Builder.Default
    private List<Map<String, Object>> history = new ArrayList<>();

    @Schema(description = "是否使用流式输出", example = "false")
    private Boolean streamOutput;

    @Schema(description = "多模态类型", example = "IMAGE", allowableValues = { "IMAGE", "VIDEO", "AUDIO" })
    private String type;

    @Schema(description = "模型名称")
    private String model;
}