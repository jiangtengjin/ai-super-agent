package com.xhh.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 基于本地的 rag 知识库
 */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    // 文档分词器
//    @Resource
//    private MyTokenTextSplitter myTokenTextSplitter;

    // 关键词增强器
    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    /**
     * 初始化向量数据库并保存文档
     *
     * @param dashscopeEmbeddingModel embedding 模型 -> 用于计算文档的向量
     * @return
     */
    @Bean
    public VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // 文档切分
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        // 自动补充关键词元信息
        List<Document> enRichDocuments = myKeywordEnricher.enrichDocuments(documents);
        // 写入向量数据库
        simpleVectorStore.doAdd(enRichDocuments);
        return simpleVectorStore;
    }

}
