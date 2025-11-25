package com.xhh.aiagent.controller;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.model.entity.User;
import com.xhh.aiagent.model.vo.ConversationVO;
import com.xhh.aiagent.service.ConversationService;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private UserService userService;

    @Resource
    private ConversationService conversationService;

    @GetMapping("/page")
    public BaseResponse<Page<ConversationVO>> getConversationByPage(@RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return ResultUtils.success(conversationService.getConversationByPage(pageSize, lastCreateTime, loginUser.getId()));
    }

    /**
     * 生成对话 ID
     *
     * @param request
     * @return
     */
    @GetMapping("/get/conversationId")
    public BaseResponse<String> generateConversation(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        String uuid = UUID.randomUUID().toString();
        return ResultUtils.success(uuid);
    }

}
