package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        String result = new TerminalOperationTool().executeTerminalCommand("ipconfig");
        Assertions.assertNotNull(result);
    }
}