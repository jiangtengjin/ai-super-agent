package com.xhh.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhh.aiagent.model.entity.Conversation;
import com.xhh.aiagent.model.request.ConversationQueryRequest;

import java.time.LocalDateTime;

/**
 * @author 机hui难得
 * @description 针对表【conversation(对话)】的数据库操作Service
 * @createDate 2025-11-22 17:33:17
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 添加对话
     *
     * @param userMessage    用户消息
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 会话ID
     */
    Long addConversation(String userMessage, String conversationId, Long userId);

    /**
     * 分页查询当前用户的对话列表（游标查询）
     *
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param userId         用户 ID
     * @return
     */
    Page<Conversation> getConversationByPage(int pageSize, LocalDateTime lastCreateTime, long userId);

    /**
     * 构造查询包装类
     *
     * @param conversationQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ConversationQueryRequest conversationQueryRequest);

}
