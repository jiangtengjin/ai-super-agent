package com.xhh.aiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.mapper.ChatHistoryMapper;
import com.xhh.aiagent.model.entity.ChatHistory;
import com.xhh.aiagent.model.entity.Conversation;
import com.xhh.aiagent.service.ChatHistoryService;
import com.xhh.aiagent.service.ConversationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author 机hui难得
* @description 针对表【chat_history(对话历史)】的数据库操作Service实现
* @createDate 2025-11-08 17:51:13
*/
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{

    @Resource
    private ConversationService conversationService;

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(String conversationId, int pageSize, LocalDateTime lastCreateTime) {
        ThrowUtils.throwIf(StrUtil.isBlank(conversationId), ErrorCode.PARAMS_ERROR, "会话 ID 不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        Conversation conversation = conversationService.getConversationByCId(conversationId);
        ThrowUtils.throwIf(conversation == null, ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        // 构建查询条件
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getConversationId, conversationId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime);
        }
        queryWrapper.orderByAsc(ChatHistory::getCreateTime);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getConversationId, conversationId);
        this.remove(queryWrapper);
    }
}




