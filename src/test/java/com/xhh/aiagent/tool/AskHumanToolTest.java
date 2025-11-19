package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AskHumanToolTest {

    @Test
    void doAsk() {
        AskHumanTool askHumanTool = new AskHumanTool();
        String result = askHumanTool.doAsk("可以告诉我你的名字吗");
        Assertions.assertNotNull(result);
    }
}