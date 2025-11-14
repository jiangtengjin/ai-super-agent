package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "txt.pdf";
        String content = "蒋腾进的 github 仓库：github/jiangtengjin";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
