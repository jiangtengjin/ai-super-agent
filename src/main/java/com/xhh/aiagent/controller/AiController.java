package com.xhh.aiagent.controller;

import com.xhh.aiagent.app.LoveApp;
import com.xhh.aiagent.exception.BusinessException;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.manus.MyManus;
import com.xhh.aiagent.model.entity.User;
import com.xhh.aiagent.service.ConversationService;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * AI 相关接口
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private UserService userService;

    @Resource
    private ConversationService conversationService;

    /**
     * 对话接口（同步，等 AI 回复完成再返回）
     */
    @GetMapping("chat_app/chat/sync")
    public String doChatWithSync(String userMessage, String chatId) {
        return loveApp.doChat(userMessage, chatId);
    }

    // =========================  SSE 流式输出  ======================================

    /**
     * 对话接口（流式输出）
     * 返回 Flux 对象，但是需要执行请求头：text/event-stream
     */
    @GetMapping(value = "chat_app/chat/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithFlux(String userMessage, String chatId) {
        return loveApp.doChatWithStream(userMessage, chatId);
    }

    /**
     * 对话接口（流式输出）
     * 返回 Flux 对象，嵌套 SSE
     */
    @GetMapping(value = "chat_app/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithSSE(String userMessage, String chatId) {
        return loveApp.doChatWithStream(userMessage, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 对话接口（流式输出）
     * 返回 SseEmitter 对象
     */
    @GetMapping(value = "chat_app/chat/SseEmitter")
    public SseEmitter doChatWithSseEmitter(String userMessage, String chatId, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        // 创建一个 SseEmitter 对象
        SseEmitter emitter = new SseEmitter(180000L); // 超时时间 3分钟
        // 获取 Flux 数据流并直接订阅
        loveApp.doChatWithStream(userMessage, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );

        // 创建对话记录
        conversationService.addConversation(userMessage, chatId, loginUser.getId());
        return emitter;
    }

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * AI 智能体接口（流式输出）
     * 返回 SseEmitter 对象
     */
    @GetMapping(value = "manus/chat")
    public SseEmitter doChatWithManus(String userMessage) {
        return new MyManus(allTools, dashscopeChatModel).runWithStream(userMessage);
    }

}
