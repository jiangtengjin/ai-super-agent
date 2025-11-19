package com.xhh.aiagent.manus;

import com.xhh.aiagent.advisor.CustomLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class MyManus extends ToolCallAgent{
    public MyManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        setName("myManus");
        setDescription("This is my Manus");
        setSystemPrompt("""
                You are myManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                Whether it's programming, information retrieval, file processing, web browsing, or human interaction (only for extreme cases),
                you can handle it all.
                """);
        setNextStepPrompt("""
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                 For complex tasks, you can break down the problem and use different tools step by step to solve it.
                 After using each tool, clearly explain the execution results and suggest the next steps.
                """);
        setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new CustomLoggerAdvisor())
                .build();
        setChatClient(chatClient);
    }
}
