<template>
  <div class="super-agent-container">
    <div class="agent-header">
      <button class="back-btn" @click="goHome">← 返回</button>
      <h1>AI 超级智能体</h1>
      <p class="subtitle">具备思考、行动和工具调用能力的智能助手</p>
    </div>

    <div class="agent-content">
      <!-- 左侧：对话历史 -->
      <div class="conversation-panel">
        <div class="panel-header">
          <h3>对话记录</h3>
          <button class="clear-btn" @click="clearConversation">清空</button>
        </div>
        <div class="conversation-list" ref="conversationList">
          <div
            v-for="(item, index) in conversationHistory"
            :key="index"
            class="conversation-item"
            :class="item.type"
          >
            <div class="conversation-icon">
              <span v-if="item.type === 'user'">👤</span>
              <span v-else-if="item.type === 'thinking'">🤔</span>
              <span v-else-if="item.type === 'action'">⚡</span>
              <span v-else-if="item.type === 'tool'">🔧</span>
              <span v-else-if="item.type === 'result'">✅</span>
              <span v-else>🤖</span>
            </div>
            <div class="conversation-content">
              <div class="conversation-type">
                {{ getTypeLabel(item.type) }}
              </div>
              <div class="conversation-text">
                {{ item.content }}
              </div>
              <div v-if="item.toolName" class="tool-info">
                工具: {{ item.toolName }}
              </div>
            </div>
          </div>
          <div v-if="isStreaming" class="conversation-item thinking">
            <div class="conversation-icon">🤔</div>
            <div class="conversation-content">
              <div class="conversation-type">思考中</div>
              <div class="streaming-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：输入区域 -->
      <div class="input-panel">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.shift.enter.exact="inputMessage += '\n'"
            placeholder="输入您的任务或问题..."
            rows="4"
            class="message-input"
            ref="inputRef"
          ></textarea>
          <div class="input-actions">
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || isStreaming"
              @click="sendMessage"
            >
              {{ isStreaming ? '处理中...' : '发送' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithManus } from '@/api/ai'
import { SSEManager } from '@/utils/sse'

interface ConversationItem {
  type: 'user' | 'thinking' | 'action' | 'tool' | 'result' | 'assistant'
  content: string
  toolName?: string
}

const router = useRouter()

const inputMessage = ref('')
const conversationHistory = ref<ConversationItem[]>([])
const isStreaming = ref(false)
const conversationList = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLTextAreaElement | null>(null)

let sseManager: SSEManager | null = null

// 将后端的事件类型映射为前端展示类型
const mapBackendType = (backendType?: string): ConversationItem['type'] => {
  switch (backendType) {
    case 'THOUGHTS':
      return 'thinking'
    case 'TOOL_SELECTION':
    case 'TOOL_CALL_INFO':
      return 'tool'
    case 'STEP_INFO':
      return 'result'
    default:
      return 'assistant'
  }
}

// 获取类型标签（中文文案）
const getTypeLabel = (type: string): string => {
  const labels: Record<string, string> = {
    user: '用户',
    thinking: '思考',
    action: '行动',
    tool: '工具调用',
    result: '阶段结果',
    assistant: '总结回复'
  }
  return labels[type] || type
}

// 解析 SSE 消息
const parseSSEMessage = (data: string): ConversationItem | null => {
  try {
    // 尝试解析 JSON
    if (data.startsWith('{')) {
      const parsed = JSON.parse(data)
      const backendType: string | undefined = parsed.type
      let content: string = parsed.content || parsed.message || ''

      // 把可能带有 data: 前缀的内容清理掉
      if (typeof content === 'string') {
        content = content.replace(/^data:\s*/gm, '').trim()
      }

      return {
        type: mapBackendType(backendType),
        content,
        toolName: parsed.toolName
      }
    }
    
    // 尝试解析特定格式的文本
    // 例如: "THINKING: xxx" 或 "ACTION: xxx" 等
    if (data.startsWith('THINKING:')) {
      return {
        type: 'thinking',
        content: data.replace('THINKING:', '').trim()
      }
    }
    if (data.startsWith('ACTION:')) {
      return {
        type: 'action',
        content: data.replace('ACTION:', '').trim()
      }
    }
    if (data.startsWith('TOOL:')) {
      const parts = data.replace('TOOL:', '').trim().split('|')
      return {
        type: 'tool',
        content: parts[1] || parts[0] || '',
        toolName: parts[0] || ''
      }
    }
    if (data.startsWith('RESULT:')) {
      return {
        type: 'result',
        content: data.replace('RESULT:', '').trim()
      }
    }
    
    // 默认作为助手回复
    return {
      type: 'assistant',
      content: data
    }
  } catch (e) {
    // 如果解析失败，作为普通文本
    return {
      type: 'assistant',
      content: data
    }
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 添加用户消息
  conversationHistory.value.push({
    type: 'user',
    content: userMessage
  })

  isStreaming.value = true
  scrollToBottom()

  // 创建 SSE 连接（后端使用 event: agentEvent）
  const eventSource = chatWithManus(userMessage)
  sseManager = new SSEManager()

  sseManager.onMessage((data: string) => {
    // 部分后端可能一次推送多行，这里按行拆分逐条解析
    const chunks = data.split('\n').filter((line) => line.trim() !== '')
    if (chunks.length === 0) {
      return
    }

    for (const chunk of chunks) {
      const parsed = parseSSEMessage(chunk)
      if (parsed) {
        // 每条消息都单独展示，方便还原“思考-行动-结果”的完整过程
        conversationHistory.value.push(parsed)
      }
    }

    scrollToBottom()
  })

  sseManager.onError((error: Error) => {
    console.error('SSE error:', error)
    conversationHistory.value.push({
      type: 'assistant',
      content: '抱歉，发生了错误，请重试。'
    })
    isStreaming.value = false
  })

  sseManager.onComplete(() => {
    isStreaming.value = false
    scrollToBottom()
  })

  // 这里显式指定监听后端的自定义事件名 agentEvent
  sseManager.connect(eventSource, 'agentEvent')
}

// 清空对话
const clearConversation = () => {
  if (sseManager) {
    sseManager.close()
  }
  conversationHistory.value = []
  isStreaming.value = false
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (conversationList.value) {
      conversationList.value.scrollTop = conversationList.value.scrollHeight
    }
  })
}

// 返回主页
const goHome = () => {
  if (sseManager) {
    sseManager.close()
  }
  router.push('/')
}

// 初始化
onMounted(() => {
  if (inputRef.value) {
    inputRef.value.focus()
  }
})

onUnmounted(() => {
  if (sseManager) {
    sseManager.close()
  }
})
</script>

<style scoped>
.super-agent-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-color);
  overflow: hidden;
}

.agent-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.back-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  transition: color 0.2s;
}

.back-btn:hover {
  color: var(--text-color);
}

.agent-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 0.25rem;
}

.subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.agent-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧对话面板 */
.conversation-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-color);
  background: var(--bg-secondary);
  overflow: hidden;
}

.panel-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-color);
}

.panel-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-color);
}

.clear-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: var(--hover-bg);
  color: var(--text-color);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.conversation-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  transition: all 0.2s;
}

.conversation-item:hover {
  box-shadow: var(--shadow);
}

.conversation-item.user {
  background: #e0e7ff;
  border-color: #c7d2fe;
}

.conversation-item.thinking {
  background: #fef3c7;
  border-color: #fde68a;
}

.conversation-item.action {
  background: #dbeafe;
  border-color: #bfdbfe;
}

.conversation-item.tool {
  background: #f3e8ff;
  border-color: #e9d5ff;
}

.conversation-item.result {
  background: #d1fae5;
  border-color: #a7f3d0;
}

.conversation-item.assistant {
  background: var(--bg-secondary);
}

.conversation-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-type {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.conversation-text {
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--text-color);
  white-space: pre-wrap;
  word-wrap: break-word;
}

.tool-info {
  margin-top: 0.5rem;
  padding: 0.375rem 0.75rem;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  font-size: 0.75rem;
  color: var(--text-secondary);
  display: inline-block;
}

.streaming-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.streaming-indicator span {
  width: 8px;
  height: 8px;
  background: var(--text-secondary);
  border-radius: 50%;
  animation: bounce 1.4s infinite;
}

.streaming-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.streaming-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* 右侧输入面板 */
.input-panel {
  width: 400px;
  padding: 1.5rem;
  border-left: 1px solid var(--border-color);
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-input {
  width: 100%;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-size: 0.875rem;
  font-family: inherit;
  resize: none;
  transition: border-color 0.2s;
  background: var(--bg-secondary);
}

.message-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.send-btn {
  padding: 0.75rem 2rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: var(--primary-hover);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 1024px) {
  .agent-content {
    flex-direction: column;
  }
  
  .input-panel {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--border-color);
  }
}
</style>

