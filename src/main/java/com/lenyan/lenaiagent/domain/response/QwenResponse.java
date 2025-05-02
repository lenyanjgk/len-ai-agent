package com.lenyan.lenaiagent.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 通义千问响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通义千问响应")
public class QwenResponse {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "助手回复内容")
    private String content;

    @Schema(description = "完成原因", example = "stop")
    private String finishReason;

    @Schema(description = "用量信息")
    private Usage usage;

    @Schema(description = "搜索信息")
    private SearchInfo searchInfo;

    @Schema(description = "错误码")
    private String code;

    @Schema(description = "错误信息")
    private String message;

    @Schema(description = "状态码")
    private Integer statusCode;

    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 用量信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用量信息")
    public static class Usage {
        @Schema(description = "输入Token数")
        private Integer inputTokens;

        @Schema(description = "输出Token数")
        private Integer outputTokens;

        @Schema(description = "总Token数")
        private Integer totalTokens;

        @Schema(description = "图像Token数")
        private Integer imageTokens;

        @Schema(description = "视频Token数")
        private Integer videoTokens;

        @Schema(description = "音频Token数")
        private Integer audioTokens;
    }

    /**
     * 搜索信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索信息")
    public static class SearchInfo {
        @Schema(description = "搜索结果列表")
        private List<SearchResult> searchResults;
    }

    /**
     * 搜索结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索结果")
    public static class SearchResult {
        @Schema(description = "网站名称")
        private String siteName;

        @Schema(description = "网站图标")
        private String icon;

        @Schema(description = "索引")
        private Integer index;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "URL")
        private String url;
    }
}