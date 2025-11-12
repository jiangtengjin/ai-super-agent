package com.xhh.aiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 * 前提：用户提示词可能不专业，比较随意
 * 作用：重写用户提示词，提高文档命中率
 */
@Component
public class MyQueryRewriter {

    private final RewriteQueryTransformer queryTransformer;

    /**
     * 初始化查询重写器
     *
     * @param dashscopeChatModel    chatModel
     */
    public MyQueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    public String rewrite(String userMessage) {
        Query query = new Query(userMessage);
        // 执行查询重写
        Query transformedMsg = this.queryTransformer.transform(query);
        // 返回重写后的提示词
        return transformedMsg.text();
    }

}
