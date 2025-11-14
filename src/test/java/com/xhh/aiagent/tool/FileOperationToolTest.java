package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String content = fileOperationTool.readFile("test.txt");
        Assertions.assertNotNull(content);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String result = fileOperationTool.writeFile("test.txt", "我是蒋腾进");
        Assertions.assertNotNull(result);
    }
}