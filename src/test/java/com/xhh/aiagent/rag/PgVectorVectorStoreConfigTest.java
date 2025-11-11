package com.xhh.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("学 Java 的蒋腾进想找工作", Map.of("Java", "工作")),
                new Document("一个 Java 开发的工作"),
                new Document("工作地点在深圳或者广州", Map.of("地点", "城市")));

        // Add the documents to PGVector
        pgVectorVectorStore.add(documents);

        // Retrieve documents similar to a query
        List<Document> results = this.pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("找工作").topK(5).build());

        Assertions.assertNotNull(results);
    }
}