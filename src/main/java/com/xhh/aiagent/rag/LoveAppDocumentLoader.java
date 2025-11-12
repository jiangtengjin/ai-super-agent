package com.xhh.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 文档读取器
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载 Markdown 文档
     * @return  文档列表
     */
    public List<Document> loadMarkdowns() {
        ArrayList<Document> documents = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/love/*.md");
            for (Resource resource : resources) {
                // 源文件名
                String filename = resource.getFilename();
                int length = filename.length();
                // 以文件名的状态作为标签
                String status = filename.substring(length - 6, length - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(false) // 是否以分割线创建文档
                        .withIncludeCodeBlock(false) // 是否读取代码块
                        .withIncludeBlockquote(false) // 是否读取引用块
                        .withAdditionalMetadata("filename", filename) // 添加文档元信息
                        .withAdditionalMetadata("status", status) // 添加文档元信息
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documents.addAll(reader.get());
            }
            log.info("成功加载：{} 条文档", documents.size());
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return documents;
    }

}
