package com.xhh.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MyQueryExpanderTest {

    @Resource
    private MyQueryExpander myQueryExpander;

    @Test
    void expand() {
        String query = "舔狗";
        List<String> expanded = myQueryExpander.expand(query);
        Assertions.assertNotNull(expanded);
    }
}