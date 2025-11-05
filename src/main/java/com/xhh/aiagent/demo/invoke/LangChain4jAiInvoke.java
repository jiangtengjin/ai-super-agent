package com.xhh.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class LangChain4jAiInvoke {

    public static void main(String[] args) {
        QwenChatModel chatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String result = chatModel.chat("你好，我是蒋腾进");
        System.out.println(result);
    }

}
