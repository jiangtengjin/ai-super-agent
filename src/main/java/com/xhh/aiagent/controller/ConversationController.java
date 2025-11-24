package com.xhh.aiagent.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.model.entity.ChatHistory;
import com.xhh.aiagent.model.entity.Conversation;
import com.xhh.aiagent.model.entity.User;
import com.xhh.aiagent.service.ChatHistoryService;
import com.xhh.aiagent.service.ConversationService;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private UserService userService;

    @Resource
    private ConversationService conversationService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @GetMapping("/page")
    public BaseResponse<Page<Conversation>> getConversationList(@RequestParam(defaultValue = "10") int pageSize,
                                                                @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return ResultUtils.success(conversationService.getConversationByPage(pageSize, lastCreateTime, loginUser.getId()));
    }

    /**
     * 根据会话 ID 查询完整的对话历史
     */
    @GetMapping("/{conversationId}/history")
    public BaseResponse<List<ChatHistory>> getConversationHistory(@PathVariable String conversationId,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(conversationId), ErrorCode.PARAMS_ERROR, "conversationId 不能为空");

        LambdaQueryWrapper<Conversation> conversationQueryWrapper = new LambdaQueryWrapper<>();
        conversationQueryWrapper.eq(Conversation::getConversationId, conversationId)
                .eq(Conversation::getUserId, loginUser.getId());
        Conversation conversation = conversationService.getOne(conversationQueryWrapper);
        ThrowUtils.throwIf(conversation == null, ErrorCode.NO_AUTH_ERROR, "会话不存在或无权限访问");

        LambdaQueryWrapper<ChatHistory> historyQueryWrapper = new LambdaQueryWrapper<>();
        historyQueryWrapper.eq(ChatHistory::getConversationId, conversationId)
                .orderByAsc(ChatHistory::getCreateTime);
        List<ChatHistory> historyList = chatHistoryService.list(historyQueryWrapper);
        return ResultUtils.success(historyList);
    }

    /**
     * 生成对话 ID
     *
     * @param request
     * @return
     */
    @GetMapping("/get/conversationId")
    public BaseResponse<String> deleteConversation(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        String uuid = UUID.randomUUID().toString();
        return ResultUtils.success(uuid);
    }

}
