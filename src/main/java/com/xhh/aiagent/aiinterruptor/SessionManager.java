package com.xhh.aiagent.aiinterruptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 会话注册中心
 */
@Component
public class SessionManager {

    /**
     * 存储 AI 会话
     */
    private final Map<String, SseEmitter> activeSessions = new ConcurrentHashMap<>();

    /**
     * 注册新会话
     *
     * @param conversationId
     * @param emitter
     */
    public void registerSession(String conversationId, SseEmitter emitter) {
        // 设置连接超时与回调，连接超时或完成后自动移除
        emitter.onCompletion(() -> removeSession(conversationId));
        emitter.onTimeout(() -> removeSession(conversationId));
        activeSessions.put(conversationId, emitter);
    }

    /**
     * 中断指定会话
     *
     * @param conversationId
     * @return
     */
    public boolean interrupt(String conversationId) {
        SseEmitter emitter = activeSessions.get(conversationId);
        if (emitter != null) {
            emitter.complete(); // 主动完成 SSE 连接
            removeSession(conversationId);
            return true;
        }
        return false; // 会话不存在或已结束
    }

    /**
     * 移除会话
     *
     * @param conversationId
     */
    public void removeSession(String conversationId) {
        activeSessions.remove(conversationId);
    }
}
