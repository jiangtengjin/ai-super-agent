package com.xhh.aiagent.rag.querytransformer;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class TextTransformerTest {

    @Resource
    private TextTransformer textTransformer;

    @Test
    void textTrans() {
        String termId = UUID.randomUUID().toString();
        String response = textTransformer.textTrans("en", "zh", "Please tell me what is the love", termId);
        Assertions.assertNotNull(response);
    }
}