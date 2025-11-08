package com.xhh.aiagent.app;

import com.xhh.aiagent.advisor.CustomLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

    /**
     * 恋爱报告
     *
     * @param title         报告标题
     * @param suggestions   建议列表
     */
    record LoveReport (String title, List<String> suggestions) {}

    /**
     * 初始化 chatClient
     *
     * @param dashscopeChatModel    指定大模型
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        InMemoryChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new CustomLoggerAdvisor()
                        // 自定义 R2 Advisor，可按需开启
//                        , new ReReadAdvisor()
                )
                .build();
    }

    /**
     * 对话
     *
     * @param message   用户提示词
     * @param chatId    对话 id，用于隔离每个用户的对话
     * @return          AI 的返回结果
     */
    public LoveReport doChat (String message, String chatId) {
        LoveReport report = chatClient.prompt()
                .user(message)
                .advisors()
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("report: {}", report);
        return report;
    }
}
