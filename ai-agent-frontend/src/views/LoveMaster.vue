<template>
  <a-layout class="love-master-layout">
    <a-layout-sider
      class="love-sider"
      :width="280"
      :collapsed-width="64"
      collapsible
      v-model:collapsed="sidebarCollapsed"
      breakpoint="lg"
    >
      <div class="sider-inner">
        <div class="sider-header" v-if="!sidebarCollapsed">
          <div>
            <h3>会话列表</h3>
            <p>点击查看历史或发起新对话</p>
          </div>
          <a-button type="primary" shape="circle" size="small" @click="openNewChatModal">
            <template #icon>
              <PlusOutlined />
            </template>
          </a-button>
        </div>
        <a-spin :spinning="conversationLoading">
          <a-list
            class="conversation-list"
            :data-source="conversationList"
            :split="false"
            :locale="{ emptyText: '暂无对话' }"
          >
            <template #renderItem="{ item }">
              <a-list-item
                class="conversation-item"
                :key="item.conversationId"
                :class="{ active: currentChatId === item.conversationId }"
                @click="selectConversation(item)"
              >
                <a-list-item-meta
                  :title="item.name || '未命名对话'"
                  :description="formatDate(item.createTime)"
                />
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </div>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="chat-header">
        <div class="header-left">
          <a-button type="text" class="collapse-trigger" @click="toggleSidebar">
            <template #icon>
              <MenuUnfoldOutlined v-if="sidebarCollapsed" />
              <MenuFoldOutlined v-else />
            </template>
          </a-button>
          <div class="header-info">
            <h2>AI 恋爱大师</h2>
            <p>{{ selectedConversation?.name || '新的对话' }}</p>
          </div>
        </div>

        <a-space>
          <a-button :loading="conversationLoading" @click="refreshConversations">刷新列表</a-button>
          <a-button type="primary" @click="openNewChatModal">
            <template #icon>
              <PlusOutlined />
            </template>
            新建对话
          </a-button>
        </a-space>
      </a-layout-header>

      <a-layout-content class="chat-content">
        <a-spin :spinning="messageLoading">
          <div class="messages-container" ref="messagesContainer">
            <a-empty v-if="messages.length === 0 && !isStreaming" description="开始和恋爱大师聊天吧" />
            <div v-else class="messages-wrapper">
              <div
                v-for="(messageItem, index) in messages"
                :key="index"
                class="message"
                :class="{
                  'user-message': messageItem.role === 'user',
                  'ai-message': messageItem.role === 'assistant'
                }"
              >
                <div class="message-bubble">
                  <div class="message-text" v-html="formatMessage(messageItem.content)"></div>
                  <div
                    v-if="messageItem.role === 'assistant' && messageItem.streaming"
                    class="streaming-indicator"
                  >
                    <span></span><span></span><span></span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-spin>

        <div
          class="composer-wrapper"
          :class="[
            composerMode === 'floating' ? 'mode-floating' : 'mode-docked',
            { collapsed: composerCollapsed }
          ]"
        >
          <transition name="fade-slide">
            <div v-if="!composerCollapsed" class="composer-card">
              <div class="composer-collapse-btn">
                <a-button type="text" size="small" @click="toggleComposerCollapse">
                  <template #icon>
                    <DownOutlined />
                  </template>
                  收起
                </a-button>
              </div>
              <div class="composer-body">
                <a-textarea
                  v-model:value="inputMessage"
                  class="message-input"
                  :auto-size="{ minRows: 1, maxRows: 4 }"
                  placeholder="输入您的问题..."
                  @pressEnter="handlePressEnter"
                  @focus="handleComposerFocus"
                  @input="handleComposerInput"
                  ref="inputRef"
                />
                <a-button
                  type="primary"
                  :disabled="!inputMessage.trim() || isStreaming"
                  @click="sendMessage"
                  class="send-btn"
                  shape="round"
                >
                  <template #icon>
                    <SendOutlined />
                  </template>
                  发送
                </a-button>
              </div>
            </div>
          </transition>
          <transition name="fade-slide">
            <div v-if="composerCollapsed" class="composer-collapsed-handle">
              <a-button shape="round" type="primary" ghost @click="toggleComposerCollapse">
                <template #icon>
                  <UpOutlined />
                </template>
                展开输入框
              </a-button>
            </div>
          </transition>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>

  <a-modal
    v-model:open="showNewChatModal"
    title="开启新的对话"
    :mask-closable="false"
    destroy-on-close
  >
    <a-form layout="vertical">
      <a-form-item label="你想聊些什么？">
        <a-textarea
          v-model:value="newChatPrompt"
          placeholder="例如：如何更好地表达关心与爱意？"
          :rows="4"
        />
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button @click="showNewChatModal = false">稍后再说</a-button>
      <a-button type="primary" :disabled="!newChatPrompt.trim()" @click="handleStartConversation">
        开始对话
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { message as messageApi } from 'ant-design-vue'
import { PlusOutlined, MenuFoldOutlined, MenuUnfoldOutlined, SendOutlined, DownOutlined, UpOutlined } from '@ant-design/icons-vue'
import { chatWithSseEmitter } from '@/api/ai'
import { fetchConversationHistory, fetchConversationList } from '@/api/conversation'
import type { ConversationRecord, ChatHistoryRecord } from '@/api/conversation'
import { SSEManager } from '@/utils/sse'

interface MessageItem {
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
}

interface ConversationSummary extends ConversationRecord {
  local?: boolean
}

const messages = ref<MessageItem[]>([])
const inputMessage = ref('')
const currentChatId = ref('')
const selectedConversation = ref<ConversationSummary | null>(null)
const conversationList = ref<ConversationSummary[]>([])
const conversationLoading = ref(false)
const messageLoading = ref(false)
const isStreaming = ref(false)
const sidebarCollapsed = ref(false)
const showNewChatModal = ref(true)
const newChatPrompt = ref('')
const composerPinned = ref(false)
const composerCollapsed = ref(false)

const messagesContainer = ref<HTMLElement | null>(null)
const inputRef = ref<{ focus: () => void } | null>(null)

const composerMode = computed(() => {
  if (composerCollapsed.value) {
    return 'collapsed'
  }
  const shouldFloat = !composerPinned.value && messages.value.length === 0 && !isStreaming.value && !inputMessage.value.trim()
  return shouldFloat ? 'floating' : 'docked'
})

let sseManager: SSEManager | null = null

const generateChatId = () => `chat_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`

const formatMessage = (content: string) =>
  content.replace(/\n/g, '<br>').replace(/ /g, '&nbsp;')

const formatDate = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const mapHistoryToMessage = (item: ChatHistoryRecord): MessageItem => {
  return {
    role: item.messageType === 'USER' ? 'user' : 'assistant',
    content: item.message
  }
}

const loadConversationHistory = async (conversationId: string) => {
  messageLoading.value = true
  messages.value = []
  try {
    const response = await fetchConversationHistory(conversationId)
    const history = response.data.data ?? []
    messages.value = history.map(mapHistoryToMessage)
    composerPinned.value = messages.value.length > 0
    composerCollapsed.value = false
    scrollToBottom()
  } catch (error) {
    messageApi.error('加载对话历史失败，请稍后再试')
  } finally {
    messageLoading.value = false
  }
}

const selectConversation = async (conversation: ConversationSummary) => {
  if (sseManager) {
    sseManager.close()
    sseManager = null
  }
  isStreaming.value = false
  currentChatId.value = conversation.conversationId
  selectedConversation.value = conversation
  await loadConversationHistory(conversation.conversationId)
}

const fetchConversations = async () => {
  conversationLoading.value = true
  try {
    const response = await fetchConversationList({ pageSize: 20 })
    conversationList.value = response.data.data?.records ?? []
    if (currentChatId.value) {
      const matched = conversationList.value.find(item => item.conversationId === currentChatId.value)
      if (matched) {
        selectedConversation.value = matched
      }
    }
  } catch (error) {
    messageApi.error('获取会话列表失败，请确认已登录')
  } finally {
    conversationLoading.value = false
  }
}

const refreshConversations = () => {
  fetchConversations()
}

const injectLocalConversation = (name: string, conversationId: string) => {
  const placeholder: ConversationSummary = {
    name,
    conversationId,
    createTime: new Date().toISOString(),
    local: true
  }
  conversationList.value = [
    placeholder,
    ...conversationList.value.filter(item => item.conversationId !== conversationId)
  ]
  selectedConversation.value = placeholder
  composerPinned.value = false
  composerCollapsed.value = false
}

const sendMessage = async (presetMessage?: string) => {
  const content = (presetMessage ?? inputMessage.value).trim()
  if (!content || isStreaming.value) return

  if (!presetMessage) {
    inputMessage.value = ''
  }

  if (!currentChatId.value) {
    currentChatId.value = generateChatId()
  }

  if (!selectedConversation.value || selectedConversation.value.conversationId !== currentChatId.value) {
    injectLocalConversation(content.slice(0, 20), currentChatId.value)
  }
  composerPinned.value = true
  composerCollapsed.value = false

  messages.value.push({
    role: 'user',
    content
  })

  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    streaming: true
  })

  isStreaming.value = true
  scrollToBottom()

  if (sseManager) {
    sseManager.close()
  }

  const eventSource = chatWithSseEmitter(content, currentChatId.value)
  sseManager = new SSEManager()
  let fullContent = ''

  sseManager.onMessage((data: string) => {
    fullContent += data
    const aiMessage = messages.value[aiMessageIndex]
    if (aiMessage) {
      aiMessage.content = fullContent
    }
    scrollToBottom()
  })

  sseManager.onError((error: Error) => {
    console.error('SSE error:', error)
    messageApi.error('对话发生错误，请稍后重试')
    const aiMessage = messages.value[aiMessageIndex]
    if (aiMessage) {
      aiMessage.content = fullContent || '抱歉，发生了错误，请稍后再试。'
      aiMessage.streaming = false
    }
    isStreaming.value = false
  })

  sseManager.onComplete(() => {
    const aiMessage = messages.value[aiMessageIndex]
    if (aiMessage) {
      aiMessage.streaming = false
    }
    isStreaming.value = false
    refreshConversations()
  })

  sseManager.connect(eventSource)
}

const handleStartConversation = () => {
  if (!newChatPrompt.value.trim()) {
    return
  }
  if (sseManager) {
    sseManager.close()
    sseManager = null
  }
  isStreaming.value = false
  messages.value = []
  currentChatId.value = generateChatId()
  const prompt = newChatPrompt.value.trim()
  newChatPrompt.value = ''
  showNewChatModal.value = false
  composerPinned.value = false
  composerCollapsed.value = false
  sendMessage(prompt)
}

const openNewChatModal = () => {
  newChatPrompt.value = ''
  showNewChatModal.value = true
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handlePressEnter = (event: KeyboardEvent) => {
  if (event.shiftKey) {
    return
  }
  event.preventDefault()
  sendMessage()
}

const handleComposerFocus = () => {
  if (!composerPinned.value && inputMessage.value.trim()) {
    composerPinned.value = true
  }
}

const handleComposerInput = () => {
  if (inputMessage.value.trim() && !composerPinned.value) {
    composerPinned.value = true
  }
}

const toggleComposerCollapse = () => {
  composerCollapsed.value = !composerCollapsed.value
}

watch(
  () => messages.value.length,
  (len) => {
    if (len > 0) {
      composerPinned.value = true
    }
  }
)

watch(showNewChatModal, (open) => {
  if (!open) {
    nextTick(() => {
      inputRef.value?.focus()
    })
  }
})

onMounted(() => {
  fetchConversations()
  nextTick(() => {
    inputRef.value?.focus()
  })
})

onUnmounted(() => {
  if (sseManager) {
    sseManager.close()
  }
})
</script>

<style scoped>
.love-master-layout {
  min-height: 100vh;
  background: var(--bg-secondary);
  overflow: hidden;
}

.love-sider {
  background: #fff;
  border-right: 1px solid var(--border-color);
}

.sider-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sider-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sider-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text-color);
}

.sider-header p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 16px;
}

.conversation-item {
  border-radius: 10px;
  transition: background 0.2s;
  cursor: pointer;
  margin-top: 12px;
  padding: 12px;
}

.conversation-item.active {
  background: rgba(99, 102, 241, 0.12);
}

.chat-header {
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 72px;
  line-height: normal;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-trigger {
  width: 40px;
  height: 40px;
}

.header-info h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.2;
}

.header-info p {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.2;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 72px);
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
  position: relative;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  padding-bottom: 220px;
}

.messages-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  width: 100%;
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.message-bubble {
  max-width: min(520px, 80%);
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.7;
  background: #fff;
  box-shadow: var(--shadow);
}

.user-message .message-bubble {
  background: var(--primary-color);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-message .message-bubble {
  background: #fff;
  border-bottom-left-radius: 4px;
}

.message-text {
  white-space: pre-wrap;
}

.streaming-indicator {
  display: inline-flex;
  gap: 4px;
  margin-left: 8px;
}

.streaming-indicator span {
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out;
}

.ai-message .streaming-indicator span {
  background: var(--text-secondary);
}

.streaming-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.streaming-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(0.85);
    opacity: 0.6;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

.composer-wrapper {
  position: absolute;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  pointer-events: none;
}

.composer-wrapper.mode-floating {
  top: 45%;
  transform: translateY(-50%);
}

.composer-wrapper.mode-docked {
  bottom: 24px;
}

.composer-wrapper.collapsed {
  bottom: 16px;
  transform: none;
  justify-content: center;
}

.composer-card {
  width: min(760px, calc(100% - 48px));
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  padding: 20px;
  pointer-events: auto;
  position: relative;
}

.composer-body {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.composer-collapse-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}

.message-input {
  flex: 1;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  padding: 12px 16px;
  transition: border-color 0.2s;
}

.message-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.send-btn {
  height: 44px;
  padding: 0 32px;
}

.composer-collapsed-handle {
  pointer-events: auto;
}

.composer-collapsed-handle button {
  box-shadow: var(--shadow);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 992px) {
  .love-master-layout {
    flex-direction: column;
  }
  .chat-content {
    height: calc(100vh - 150px);
  }
  .input-area {
    padding: 16px;
  }
}
</style>

