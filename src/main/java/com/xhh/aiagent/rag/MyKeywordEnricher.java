package com.xhh.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关键字增强器
 */
@Component
class MyKeywordEnricher {

    @Resource
    private  ChatModel dashscopeChatModel;

    /**
     * 初始化 KeywordMetadataEnricher
     *
     * @param documents     需要进行关键字增强的文档
     * @return              增强后的文档列表
     */
    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        return enricher.apply(documents);
    }
}