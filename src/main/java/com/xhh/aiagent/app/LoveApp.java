package com.xhh.aiagent.app;

import com.xhh.aiagent.advisor.CustomLoggerAdvisor;
import com.xhh.aiagent.advisor.UserMessageCheckAdvisor;
import com.xhh.aiagent.chatmemory.InDBChatMemory;
import com.xhh.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.xhh.aiagent.rag.MyQueryRewriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
        扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。围绕单身、恋爱、已婚三种状态提问：
        单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。
        引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。
    """;

    // 恋爱助手系统提示词
    @Value("classpath:/prompt/love-assistant-system-prompt.txt")
    private Resource systemPromptResource;

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
    public LoveApp(ChatModel dashscopeChatModel, InDBChatMemory inDBChatMemory) {
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inDBChatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new CustomLoggerAdvisor(),
                        // 敏感词校验
                        new UserMessageCheckAdvisor()
                )
                .build();
    }

    /**
     * 对话（结构化输出）
     *
     * @param message   用户提示词
     * @param chatId    对话 id，用于隔离每个用户的对话
     * @return          AI 的返回结果
     */
    public LoveReport doChat (String message, String chatId) {
        LoveReport report = chatClient.prompt()
                // 系统提示词
                .system(systemPromptResource)
                .user(message)
                .advisors()
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("report: {}", report);
        return report;
    }

    @jakarta.annotation.Resource
    private Advisor loveAppRagCloudAdvisor;

    @jakarta.annotation.Resource
    private VectorStore loveAppVectorStore;

    @jakarta.annotation.Resource
    private MyQueryRewriter myQueryRewriter;

//    @jakarta.annotation.Resource
//    private VectorStore pgVectorVectorStore;

    /**
     * 对话（使用 RAG 检索知识增强）
     *
     * @param message   用户提示词
     * @param chatId    对话 id，用于隔离每个用户的对话
     * @return          AI 的返回结果
     */
    public String doChatWithRag (String message, String chatId) {
        // 重写用户提示词
//        String rewriteMsg = myQueryRewriter.rewrite(message);
        ChatResponse response = chatClient.prompt()
                // 系统提示词
                .system(systemPromptResource)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new CustomLoggerAdvisor())
                // 应用检索知识增强（云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                // 应用检索知识增强（本地知识库服务）
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 使用工厂类创建支持过滤的 advisor
                .advisors(LoveAppRagCustomAdvisorFactory.createCustomAdvisor(loveAppVectorStore, "单身"))
                // 应用检索知识增强（向量数据库）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String result = response.getResult().getOutput().getText();
        log.info("result: {}", result);
        return result;
    }

    @jakarta.annotation.Resource
    private ToolCallback[] allTools;

    /**
     * 对话（使用自定义工具）
     *
     * @param message   用户提示词
     * @param chatId    对话 id，用于隔离每个用户的对话
     * @return          AI 的返回结果
     */
    public String doChatWithTool(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new CustomLoggerAdvisor()) // 开启日志
                .tools(allTools) // 使用自定义工具
                .call()
                .chatResponse();
        String result = response.getResult().getOutput().getText();
        log.info("result: {}", result);
        return result;
    }

}
