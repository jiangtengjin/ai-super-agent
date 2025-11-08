package com.xhh.aiagent.converter;

import com.xhh.aiagent.model.entity.ChatHistory;
import org.springframework.ai.chat.messages.*;

import java.util.List;

/**
 * 消息转换器
 * Message <=> ChatHistory
 */
public class MessageConverter {

    /**
     * 将 Message 转换成 ChatHistory
     * @param message           Message 对象
     * @param conversationId    对话 Id
     * @return                  转换之后的 ChatHistory
     */
    public static ChatHistory toChatHistory(Message message, String conversationId) {
        return  ChatHistory.builder()
                .conversationId(conversationId)
                .messageType(message.getMessageType())
                .message(message.getText())
                .build();
    }

    /**
     * 将 ChatHistory 转换成 Message
     * @param chatHistory    ChatHistory 对象
     * @return               转换之后的 Message
     */
    public static Message toMessage(ChatHistory chatHistory) {
        MessageType messageType = chatHistory.getMessageType();
        String message = chatHistory.getMessage();
        return switch (messageType) {
            case USER -> new UserMessage(message);
            case ASSISTANT -> new AssistantMessage(message);
            case SYSTEM -> new SystemMessage(message);
            case TOOL -> new ToolResponseMessage(List.of());
        };
    }

}
