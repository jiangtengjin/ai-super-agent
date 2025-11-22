package com.xhh.aiagent.controller;

import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.exception.BusinessException;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.manager.captcha.CaptchaManager;
import com.xhh.aiagent.manager.captcha.model.vo.ImageCodeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaManager captchaManager;

    @GetMapping("/image/code")
    public BaseResponse<ImageCodeVO> getImageCode() {
        try {
            ImageCodeVO imageCode = captchaManager.getImageCode();
            return ResultUtils.success(imageCode);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成验证码失败");
        }
    }
}
