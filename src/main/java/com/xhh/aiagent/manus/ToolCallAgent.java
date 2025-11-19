package com.xhh.aiagent.manus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.xhh.aiagent.model.enums.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ToolCallAgent extends ReActAgent{

    // 可用的工具列表
    private final ToolCallback[] availableTools;

    // 保存了工具调用信息的响应
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private ToolCallingManager toolCallingManager;

    // 禁止内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     *  处理当前状态并决定下一步行动
     *
     * @return 是否需要行动， true 表示需要， false 表示不需要
     */
    @Override
    public boolean think() {
        List<Message> memory = getMemory();
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            memory.add(userMessage);
        }

        Prompt prompt = new Prompt(memory, chatOptions);
        try {
            // 获取工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于 act
            toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示消息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info("✨ {}'s thoughts: {}", getName(), result);
            log.info("🛠️ {} selected {} tools to use", getName(), toolCalls.size());
            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> String.format("🧰 Tools being prepared: {%s}\n Tool arguments: {%s}",
                            toolCall.name(), toolCall.arguments())
                    ).collect(Collectors.joining("\n"));
            log.info("tool call info: {}", toolCallInfo);
            if (toolCalls.isEmpty()) {
                // 只有不调用工具时，才记录助手消息
                memory.add(assistantMessage);
                return false;
            }
            // 需要调用工具时，无需记录助手消息，因为工具调用会自动记录
            return true;
        } catch (Exception e) {
            // 记录错误日志已经返回助手消息
            log.error("🚨 Oops! The {}'s thinking process hit a snag: {}", getName(), e.getMessage());
            memory.add(new AssistantMessage("Error encountered while processing: {" + e.getMessage() + "}"));
            return false;
        }
    }

    /**
     * 执行决定的行动
     *
     * @return  行动执行结果
     */
    @Override
    public String act() {
        // 如果没有工具调用，则返回
        if (!toolCallChatResponse.hasToolCalls()) {
            return "No content or commands to execute";
        }

        // 调用工具
        Prompt prompt = new Prompt(getMemory(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文, conversationHistory 已经包含助手消息和工具调用返回的结果
        List<Message> conversationHistory = toolExecutionResult.conversationHistory();
        setMemory(conversationHistory);
        // 当前工具的调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(conversationHistory);
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> String.format("🎯 Tool '{%s}' completed its mission! Result: {%s}",
                        response.name(), response.responseData())
                ).collect(Collectors.joining("\n"));
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "terminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;
    }
}
