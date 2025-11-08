package com.xhh.aiagent.advisor;

import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户提示词校验 Advisor
 * 检查用户提示词中是否包含违禁词
 */
public class UserMessageCheckAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
    );

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    private void check(String userMessage) {
        // 检查输入长度
        ThrowUtils.throwIf(userMessage.length() > 1000,
                ErrorCode.PARAMS_ERROR, "输入内容过长，请控制在1000字以内");

        // 检查是否为空
        ThrowUtils.throwIf(userMessage.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "输入内容不能为空");

        // 检查敏感词
        String lowerInput = userMessage.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            ThrowUtils.throwIf(lowerInput.contains(sensitiveWord.toLowerCase()),
                    ErrorCode.PARAMS_ERROR, "输入包含不当内容，请修改后重试");
        }

        // 检查注入攻击模式
        for (Pattern pattern : INJECTION_PATTERNS) {
            ThrowUtils.throwIf(pattern.matcher(userMessage).find(),
                    ErrorCode.PARAMS_ERROR, "检测到恶意输入，请求被拒绝");
        }
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        this.check(advisedRequest.userText());
        return advisedRequest;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
