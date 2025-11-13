package com.xhh.aiagent.rag.documentreader;

import com.alibaba.cloud.ai.document.TextDocumentParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class GitHubDocumentReaderTest {

    // 仅读
    @Value("${github.access.token}")
    private String accessToken;

    @Test
    void testGitHubDocumentReader() throws IOException {
        GitHubResource gitHubResource = GitHubResource.builder()
                .gitHub(GitHub.connect("jiangtengjin", accessToken))
                .apiUrl("https://github.com/jiangtengjin/ai-code.git")
                .owner("jiangtengjin")
                .branch("master")
                .repo("ai-code")
                .path("ai-code-microservice/ai-code-user/src/main/java/com/xhh/aicode/captcha/config/CaptchaConfig.java")
                .build();
        GitHubDocumentReader gitHubDocumentReader =
                new GitHubDocumentReader(gitHubResource, new TextDocumentParser());
        List<Document> documents = gitHubDocumentReader.get();
        Assertions.assertNotNull(documents);
    }
}