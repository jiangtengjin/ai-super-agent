package com.xhh.aiagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.model.entity.ChatHistory;
import com.xhh.aiagent.model.entity.User;
import com.xhh.aiagent.service.ChatHistoryService;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("chat/history")
public class ChatHistoryController {

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param conversationId 会话 ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/conversation/{conversationId}")
    public BaseResponse<Page<ChatHistory>> listChatHistory(@PathVariable String conversationId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(conversationId, pageSize, lastCreateTime);
        return ResultUtils.success(result);
    }
}
