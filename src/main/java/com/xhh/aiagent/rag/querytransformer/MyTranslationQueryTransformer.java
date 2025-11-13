package com.xhh.aiagent.rag.querytransformer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 自定义翻译查询转换器
 * 使用第三方 API 进行翻译替换使用 AI 进行翻译，降低成本
 */
@Slf4j
public class MyTranslationQueryTransformer implements QueryTransformer {

    /**
     * 翻译成目标语言
     */
    private final String targetLanguage;

    /**
     * 翻译器
     */
    private final TextTransformer textTransformer;

    public MyTranslationQueryTransformer(String targetLanguage, TextTransformer textTransformer) {
        this.targetLanguage = targetLanguage;
        this.textTransformer = textTransformer;
    }

    @Override
    public Query transform(Query query) {
        Assert.notNull(query, "query cannot be null");
        log.debug("Translating query to target language: {}", this.targetLanguage);
        // 翻译
        String termId = UUID.randomUUID().toString();
        String translatedQueryText = textTransformer.textTrans("zh", targetLanguage, query.text(), termId);
        if (!StringUtils.hasText(translatedQueryText)) {
            log.warn("Query translation result is null/empty. Returning the input query unchanged.");
            return query;
        } else {
            return query.mutate().text(translatedQueryText).build();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String targetLanguage;

        private TextTransformer textTransformer;

        private Builder() {
        }

        public Builder targetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
            return this;
        }

        public Builder textTransformer(TextTransformer textTransformer) {
            this.textTransformer = textTransformer;
            return this;
        }

        public MyTranslationQueryTransformer build() {
            return new MyTranslationQueryTransformer(this.targetLanguage, this.textTransformer);
        }
    }

}
