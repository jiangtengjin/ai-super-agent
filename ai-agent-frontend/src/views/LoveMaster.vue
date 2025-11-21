<template>
  <div class="love-master-container">
    <!-- 左侧边栏：对话历史 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <button class="new-chat-btn" @click="createNewChat">
          <span>+</span> 新建对话
        </button>
      </div>
      <div class="chat-history">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="chat-item"
          :class="{ active: currentChatId === chat.id }"
          @click="switchChat(chat.id)"
        >
          <span class="chat-title">{{ chat.title || `对话 ${chat.id.slice(0, 8)}` }}</span>
          <button class="delete-btn" @click.stop="deleteChat(chat.id)">×</button>
        </div>
      </div>
    </div>

    <!-- 中间：聊天区域 -->
    <div class="chat-area">
      <div class="chat-header">
        <button class="back-btn" @click="goHome">← 返回</button>
        <h2>AI 恋爱大师</h2>
      </div>
      
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">💕</div>
          <h3>开始新的对话</h3>
          <p>我是您的恋爱咨询助手，有什么问题可以问我</p>
        </div>
        
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message"
          :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }"
        >
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div v-if="message.role === 'assistant' && message.streaming" class="streaming-indicator">
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.shift.enter.exact="inputMessage += '\n'"
            placeholder="输入您的问题..."
            rows="1"
            class="message-input"
            ref="inputRef"
          ></textarea>
          <button
            class="send-btn"
            :disabled="!inputMessage.trim() || isStreaming"
            @click="sendMessage"
          >
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithSseEmitter } from '@/api/ai'
import { SSEManager } from '@/utils/sse'

interface Message {
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
}

interface ChatHistoryItem {
  id: string
  title: string
  lastMessage?: string
}

const router = useRouter()

const inputMessage = ref('')
const messages = ref<Message[]>([])
const currentChatId = ref('')
const chatHistory = ref<ChatHistoryItem[]>([])
const isStreaming = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLTextAreaElement | null>(null)

let sseManager: SSEManager | null = null

// 生成 chatId
const generateChatId = (): string => {
  return `chat_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 创建新对话
const createNewChat = () => {
  currentChatId.value = generateChatId()
  messages.value = []
  chatHistory.value.unshift({
    id: currentChatId.value,
    title: '新对话'
  })
}

// 切换对话
const switchChat = (chatId: string) => {
  if (sseManager) {
    sseManager.close()
  }
  currentChatId.value = chatId
  // TODO: 从后端加载历史消息
  messages.value = []
}

// 删除对话
const deleteChat = (chatId: string) => {
  const index = chatHistory.value.findIndex(chat => chat.id === chatId)
  if (index > -1) {
    chatHistory.value.splice(index, 1)
  }
  if (currentChatId.value === chatId) {
    if (chatHistory.value.length > 0) {
      switchChat(chatHistory.value[0].id)
    } else {
      createNewChat()
    }
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 如果没有当前对话，创建新对话
  if (!currentChatId.value) {
    createNewChat()
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage
  })

  // 添加 AI 消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    streaming: true
  })

  isStreaming.value = true
  scrollToBottom()

  // 创建 SSE 连接
  const eventSource = chatWithSseEmitter(userMessage, currentChatId.value)
  sseManager = new SSEManager()
  
  let fullContent = ''

  sseManager.onMessage((data: string) => {
    fullContent += data
    messages.value[aiMessageIndex].content = fullContent
    scrollToBottom()
  })

  sseManager.onError((error: Error) => {
    console.error('SSE error:', error)
    messages.value[aiMessageIndex].content = fullContent || '抱歉，发生了错误，请重试。'
    messages.value[aiMessageIndex].streaming = false
    isStreaming.value = false
  })

  sseManager.onComplete(() => {
    messages.value[aiMessageIndex].streaming = false
    isStreaming.value = false
    
    // 更新对话历史标题（使用第一条用户消息）
    const chat = chatHistory.value.find(c => c.id === currentChatId.value)
    if (chat && chat.title === '新对话') {
      chat.title = userMessage.slice(0, 20) + (userMessage.length > 20 ? '...' : '')
    }
  })

  sseManager.connect(eventSource)
}

// 格式化消息（支持换行）
const formatMessage = (content: string): string => {
  return content
    .replace(/\n/g, '<br>')
    .replace(/ /g, '&nbsp;')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
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
  createNewChat()
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
.love-master-container {
  display: flex;
  height: 100vh;
  background: var(--bg-color);
}

/* 左侧边栏 */
.sidebar {
  width: 260px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.new-chat-btn {
  width: 100%;
  padding: 0.75rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.new-chat-btn:hover {
  background: var(--primary-hover);
}

.new-chat-btn span {
  font-size: 1.25rem;
  line-height: 1;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.chat-item {
  padding: 0.75rem;
  margin-bottom: 0.25rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background 0.2s;
  position: relative;
}

.chat-item:hover {
  background: var(--hover-bg);
}

.chat-item.active {
  background: var(--primary-color);
  color: white;
}

.chat-title {
  flex: 1;
  font-size: 0.875rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  opacity: 0;
  background: transparent;
  border: none;
  color: inherit;
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  padding: 0.5rem;
  transition: color 0.2s;
}

.back-btn:hover {
  color: var(--text-color);
}

.chat-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-color);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.message {
  display: flex;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
}

.ai-message {
  align-self: flex-start;
}

.message-content {
  padding: 1rem 1.25rem;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-content {
  background: var(--primary-color);
  color: white;
}

.ai-message .message-content {
  background: var(--bg-secondary);
  color: var(--text-color);
}

.message-text {
  white-space: pre-wrap;
}

.streaming-indicator {
  display: inline-block;
  margin-left: 4px;
}

.streaming-indicator span {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: var(--text-secondary);
  border-radius: 50%;
  animation: blink 1.4s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.input-area {
  padding: 1.5rem;
  border-top: 1px solid var(--border-color);
  background: var(--bg-color);
}

.input-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-size: 0.875rem;
  font-family: inherit;
  resize: none;
  max-height: 200px;
  overflow-y: auto;
  transition: border-color 0.2s;
}

.message-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.send-btn {
  padding: 0.75rem 1.5rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: var(--primary-hover);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }
  
  .message {
    max-width: 90%;
  }
}
</style>

