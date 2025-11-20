package com.xhh.aiagent.manus;


import cn.hutool.core.util.StrUtil;
import com.xhh.aiagent.exception.ManusStateException;
import com.xhh.aiagent.model.enums.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤执行循环的基本步骤
 * 子类必须实现 step 方法
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 工具描述
    private String description;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // 会话上下文
    private List<Message> memory = new ArrayList<>();

    // 重复阈值
    private int duplicateThreshold = 2;

    /**
     * 运行代理
     *
     * @param userPrompt    用户提示词
     * @return              执行结果
     */
    public String run (String userPrompt) {
        if (state != AgentState.IDLE) {
            throw new ManusStateException("Cannot run agent from state: " + state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new ManusStateException("Cannot run agent from empty user prompt");
        }
        // 更新状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        memory.add(new UserMessage(userPrompt));
        try {
            // 保存结果列表
            List<String> results = new ArrayList<>();
            while (currentStep < maxSteps && state != AgentState.FINISHED) {
                currentStep++;
                log.info("Executing step {}/{}", currentStep, maxSteps);
                // 单步执行
                String stepResult = step();
                if (isStuck()) {
                    handleStuckState();
                }
                stepResult = String.format("Step %d result: %s", currentStep, stepResult);
                results.add(stepResult);
            }
            // 判断是否超出最大步骤
            if (currentStep >= maxSteps) {
                currentStep = 0;
                state = AgentState.FINISHED;
                String terminated = String.format("Terminated: Reached max steps ({%d})", maxSteps);
                results.add(terminated);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error to execute agent",e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt    用户提示词
     * @return              执行结果
     */
    public SseEmitter runWithStream (String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L); // 超时时间 5 分钟
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (state != AgentState.IDLE) {
                    emitter.send("错误：无法运行代理，因为" + state);
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误：无法运行代理，因为用户提示词为空");
                    emitter.complete();
                    return;
                }
            } catch (IOException e) {
                state = AgentState.ERROR;
                emitter.completeWithError(e);
            }
            // 更新状态
            state = AgentState.RUNNING;
            // 记录消息上下文
            memory.add(new UserMessage(userPrompt));
            try {
                while (currentStep < maxSteps && state != AgentState.FINISHED) {
                    currentStep++;
                    log.info("Executing step {}/{}", currentStep, maxSteps);
                    // 单步执行
                    String stepResult = step();
                    if (isStuck()) {
                        handleStuckState();
                    }
                    stepResult = String.format("步骤 %d 结果: %s", currentStep, stepResult);
                    emitter.send(stepResult);
                }
                // 判断是否超出最大步骤
                if (currentStep >= maxSteps) {
                    currentStep = 0;
                    state = AgentState.FINISHED;
                    String terminated = String.format("终止: 达到最大步骤 ({%d})", maxSteps);
                    emitter.send(terminated);
                }
                // 正常完成
                emitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("智能体运行失败：",e);
                emitter.completeWithError(e);
            } finally {
                // 清理资源
                cleanup();
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            state = AgentState.ERROR;
            cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
            }
            cleanup();
            log.info("Dash finished, SSE connection completed");
        });

        return emitter;
    }

    /**
     * 执行单个步骤
     * 必须由子类来实现这个方法
     * @return  步骤执行结果
     */
    protected abstract String step ();

    /**
     * 处理卡住状态
     */
    private void handleStuckState() {
        String stuckPrompt = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already attempted.";
        nextStepPrompt = String.format("stuckPrompt\n%s", stuckPrompt);
        log.warn("Agent detected stuck state. Added prompt: {}", stuckPrompt);
    }

    /**
     * 检查 agent 在执行循环中是否被重复内容卡住
     * @param
     * @return
     */
    private boolean isStuck() {
        if (memory.size() < 2) {
            return false;
        }

        // 获取最后一条消息
        Message lastMsg = memory.getLast();
        // 如果最后一条消息的内容为空，直接返回 false
        if (StrUtil.isBlank(lastMsg.getText())) {
            return false;
        }
        // 计算重复的消息数量
        int duplicateCount = 0;
        // 创建消息列表的副本（排除最后一条消息）
        List<Message> messages = new ArrayList<>(memory);
        messages.removeLast(); // 移除最后一条消息

        // 反向遍历（从最新到最旧）
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg.getMessageType().equals(MessageType.ASSISTANT) &&
                    lastMsg.getText().equals(msg.getText())) {
                duplicateCount++;
            }
        }
        return duplicateCount >= duplicateThreshold;
    }

    /**
     * 清理资源
     */
    protected void cleanup(){
        // 子类可以重写此方法来清理资源
    }

}
