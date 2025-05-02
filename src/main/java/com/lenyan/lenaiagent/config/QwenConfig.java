package com.lenyan.lenaiagent.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通义千问配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qwen")
public class QwenConfig {

    /**
     * 通义千问API密钥
     */
    @Value("${qwen.api-key}")
    private String apiKey;

    /**
     * 文本模型名称
     */
    private String textModel = "qwen-plus";

    /**
     * 视觉模型名称
     */
    private String visionModel = "qwen-vl-plus";

    /**
     * 视频模型名称
     */
    private String videoModel = "qwen-vl-max-latest";

    /**
     * 音频模型名称
     */
    private String audioModel = "qwen2-audio-instruct";

    /**
     * 是否开启流式输出
     */
    private boolean streamOutput = false;

    /**
     * 是否使用联网搜索
     */
    private boolean enableSearch = false;
}