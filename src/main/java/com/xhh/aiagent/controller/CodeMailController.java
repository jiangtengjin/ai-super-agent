package com.xhh.aiagent.controller;

import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.manager.CodeMailManager;
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

    @GetMapping("/get/{to}")
    public BaseResponse<String> getMailCode(@PathVariable String to) {
        return ResultUtils.success(codeMailManager.sendMail(to));
    }

}
