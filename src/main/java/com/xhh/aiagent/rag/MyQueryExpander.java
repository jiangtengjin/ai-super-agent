package com.xhh.aiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询扩展器
 * 作用：根据用户提示词进行扩展，提高相关文档的召回率
 */
@Component
public class MyQueryExpander {

    private final MultiQueryExpander multiQueryExpander;

    /**
     * 初始化查询扩展器
     *
     * @param dashscopeChatModel
     */
    public MyQueryExpander(ChatModel dashscopeChatModel){
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        this.multiQueryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                .includeOriginal(false) // 不包含原始查询
                .numberOfQueries(3) // 生成 3 个查询变体
                .build();
    }

    public List<String> expand(String userMsg){
        ArrayList<String> expandedUserMsg = new ArrayList<>();
        Query query = new Query(userMsg);
        // 收集扩展后的提示词
        for (Query expandedQuery : this.multiQueryExpander.expand(query)) {
            expandedUserMsg.add(expandedQuery.text());
        }
        return expandedUserMsg;
    }

}
