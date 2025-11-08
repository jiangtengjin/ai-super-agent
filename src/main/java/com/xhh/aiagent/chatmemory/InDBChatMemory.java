package com.xhh.aiagent.chatmemory;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xhh.aiagent.converter.MessageConverter;
import com.xhh.aiagent.model.entity.ChatHistory;
import com.xhh.aiagent.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 MySQL 的对话持久化
 */
@Component
@RequiredArgsConstructor
public class InDBChatMemory implements ChatMemory {


    private final ChatHistoryService chatHistoryService;

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 1. 将 Message 转换成 ChatHistory
        List<ChatHistory> list = messages.stream()
                .map(message ->
                        MessageConverter.toChatHistory(message, conversationId))
                .toList();
        // 2. 保存到数据库
        chatHistoryService.saveBatch(list);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        // 查询最近 lastN 条记录
        queryWrapper.eq(ChatHistory::getConversationId, conversationId)
                .orderByDesc(ChatHistory::getCreateTime)
                .last(lastN > 0, "LIMIT " + lastN);
        List<ChatHistory> list = chatHistoryService.list(queryWrapper);

        // 如果没有记录，返回空列表
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 按时间顺序返回
        CollUtil.reverse(list);

        return list.stream().map(MessageConverter::toMessage).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getConversationId, conversationId);
        chatHistoryService.remove(queryWrapper);
    }
}
