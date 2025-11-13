package com.xhh.aiagent.rag.querytransformer;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyTranslationQueryTransformerTest {

    @Resource
    private TextTransformer textTransformer;
    @Test
    void transform() {
        Query query = new Query("你好");
        MyTranslationQueryTransformer translationQueryTransformer = MyTranslationQueryTransformer.builder()
                .targetLanguage("en")
                .textTransformer(textTransformer)
                .build();
        Query transformedQuery = translationQueryTransformer.transform(query);
        Assertions.assertNotNull(transformedQuery);
    }
}