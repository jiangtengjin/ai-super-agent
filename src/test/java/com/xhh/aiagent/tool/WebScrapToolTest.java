package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebScrapToolTest {

    @Test
    void doScrap() {
        String result = new WebScrapTool().doScrap("https://www.baidu.com");
        Assertions.assertNotNull(result);
    }
}