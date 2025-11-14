package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search.api_key}")
    private String apiKey;

    @Test
    void searchContentFromWeb() {
        String result = new WebSearchTool(apiKey).searchContentFromWeb("java");
        Assertions.assertNotNull(result);
    }
}