package com.xhh.aiagent.controller;

import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.manager.codemail.CodeMailManager;
import com.xhh.aiagent.ratelimiter.annotation.RateLimit;
import com.xhh.aiagent.ratelimiter.enums.RateLimitType;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/code/mail")
public class CodeMailController {

    @Resource
    private CodeMailManager codeMailManager;

    @RateLimit(limitType = RateLimitType.USER, rate = 5, interval = 60)
    @GetMapping("/get/{to}")
    public BaseResponse<String> getMailCode(@PathVariable String to) {
        return ResultUtils.success(codeMailManager.sendMail(to));
    }

}
