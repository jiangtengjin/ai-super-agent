import api from './index'

/**
 * AI 恋爱大师 - SSE 流式对话
 */
export function chatWithSseEmitter(userMessage: string, chatId: string): EventSource {
  const params = new URLSearchParams({
    userMessage,
    chatId
  })
  return new EventSource(`/api/ai/chat_app/chat/SseEmitter?${params.toString()}`)
}

/**
 * AI 超级智能体 - SSE 流式对话
 */
export function chatWithManus(userMessage: string): EventSource {
  const params = new URLSearchParams({
    userMessage
  })
  return new EventSource(`/api/ai/manus/chat?${params.toString()}`)
}

