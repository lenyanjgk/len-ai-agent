package com.lenyan.lenaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 AI 的文档元信息增强器（为文档补充关键词元信息）
 */
@Component
public class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 为文档列表添加关键词元信息，提升可搜索性
     *
     * @param documents 待增强的文档列表
     * @return 增强后的文档列表
     */
    public List<Document> enrichDocuments(List<Document> documents) {
        // 创建KeywordMetadataEnricher实例，使用AI模型提取关键词
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        // 执行文档增强操作
        return keywordMetadataEnricher.apply(documents);
    }
}
