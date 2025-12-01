package com.xhh.aiagent.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhh.aiagent.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
* @author 机hui难得
* @description 针对表【chat_history(对话历史)】的数据库操作Service
* @createDate 2025-11-08 17:51:13
*/
public interface ChatHistoryService extends IService<ChatHistory> {


    Page<ChatHistory> listAppChatHistoryByPage(String conversationId, int pageSize, LocalDateTime lastCreateTime);

    /**
     * 根据会话 ID 删除对话历史
     *
     * @param conversationId
     */
    void deleteByConversationId(String conversationId);
}
