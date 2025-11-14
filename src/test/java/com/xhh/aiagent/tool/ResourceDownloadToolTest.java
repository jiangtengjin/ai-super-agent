package com.xhh.aiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceDownloadToolTest {

    @Test
    void doDownload() {
        String result = new ResourceDownloadTool().doDownload(
                "https://ai-code-1372346116.cos.ap-guangzhou.myqcloud.com/screenshots/2025/10/26/564a80a8_compressed.jpg",
                "logo.jpg");
        Assertions.assertNotNull(result);
    }
}