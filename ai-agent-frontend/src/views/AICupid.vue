<template>
  <div class="cupid-container">
    <!-- 左侧对话历史 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>对话历史</h3>
        <button @click="createNewChat" class="new-chat-btn">+</button>
      </div>
      <div class="chat-history">
        <div 
          v-for="chat in chatHistory" 
          :key="chat.id" 
          :class="['chat-item', { active: currentChatId === chat.id }]"
          @click="switchChat(chat.id)"
        >
          <div class="chat-preview">
            <div class="chat-title">{{ chat.title || '新对话' }}</div>
            <div class="chat-subtitle">{{ chat.lastMessage || '开始新的对话' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 中间聊天区域 -->
    <div class="chat-container">
      <div class="chat-header">
        <h2>AI 恋爱大师 💘</h2>
      </div>
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="currentChatMessages.length === 0" class="empty-state">
          <p>你好！我是AI恋爱大师，有什么可以帮你的吗？</p>
        </div>
        <div 
          v-for="(message, index) in currentChatMessages" 
          :key="index"
          :class="['message', message.role]"
        >
          <div class="message-content">{{ message.content }}</div>
        </div>
        <div v-if="isTyping" class="message ai typing">
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
      <div class="chat-input-area">
        <textarea
          v-model="userInput"
          @keyup.enter.ctrl="sendMessage"
          @keyup.enter.meta="sendMessage"
          placeholder="输入您的问题，按 Ctrl+Enter 发送..."
          :disabled="isTyping"
          ref="inputTextarea"
        ></textarea>
        <button 
          @click="sendMessage" 
          :disabled="isTyping || !userInput.trim()"
          class="send-btn"
        >发送</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AICupid',
  data() {
    return {
      chatHistory: [],
      currentChatId: null,
      currentChatMessages: [],
      userInput: '',
      isTyping: false,
      eventSource: null,
      // 模拟数据，用于展示效果
      mockChatHistory: [
        {
          id: 'mock-1',
          title: '如何表白',
          lastMessage: '明天我要向暗恋的人表白了，紧张...'
        },
        {
          id: 'mock-2',
          title: '情侣吵架怎么办',
          lastMessage: '我和男朋友因为小事吵架了...'
        }
      ]
    }
  },
  mounted() {
    // 初始化模拟数据
    this.chatHistory = [...this.mockChatHistory]
    if (this.chatHistory.length > 0) {
      this.switchChat(this.chatHistory[0].id)
    }
  },
  beforeUnmount() {
    // 关闭SSE连接
    if (this.eventSource) {
      this.eventSource.close()
    }
  },
  methods: {
    // 创建新对话
    createNewChat() {
      const newChatId = 'chat-' + Date.now()
      const newChat = {
        id: newChatId,
        title: '新对话',
        lastMessage: ''
      }
      this.chatHistory.unshift(newChat)
      this.switchChat(newChatId)
    },
    
    // 切换对话
    switchChat(chatId) {
      this.currentChatId = chatId
      // 这里应该从存储中加载对应chatId的消息
      // 暂时清空消息列表
      this.currentChatMessages = []
      // 滚动到底部
      this.$nextTick(() => this.scrollToBottom())
    },
    
    // 发送消息
    sendMessage() {
      if (!this.userInput.trim() || this.isTyping) return
      
      const userMessage = {
        role: 'user',
        content: this.userInput.trim()
      }
      
      this.currentChatMessages.push(userMessage)
      
      // 更新聊天历史中的最后一条消息
      this.updateChatHistory(userMessage.content)
      
      // 清空输入框
      this.userInput = ''
      this.isTyping = true
      
      // 滚动到底部
      this.$nextTick(() => this.scrollToBottom())
      
      // 调用SSE接口
      this.callSSEChat(userMessage.content)
    },
    
    // 调用SSE聊天接口
    callSSEChat(message) {
      // 如果有旧的连接，先关闭
      if (this.eventSource) {
        this.eventSource.close()
      }
      
      // 构建请求参数
      const params = new URLSearchParams()
      params.append('prompt', message)
      params.append('chatId', this.currentChatId)
      
      // 创建AI响应消息对象
      let aiResponse = {
        role: 'ai',
        content: ''
      }
      this.currentChatMessages.push(aiResponse)
      
      try {
        // 创建EventSource连接
        this.eventSource = new EventSource(`http://localhost:8123/api/ai/doChatWithSseEmitter?${params.toString()}`)
        
        // 处理消息事件
        this.eventSource.onmessage = (event) => {
          const data = event.data
          if (data) {
            aiResponse.content += data
            this.$nextTick(() => this.scrollToBottom())
          }
        }
        
        // 处理连接结束
        this.eventSource.onclose = () => {
          this.isTyping = false
          // 更新聊天历史中的标题和最后消息
          this.updateChatHistory(aiResponse.content, true)
        }
        
        // 处理错误
        this.eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          this.isTyping = false
          // 如果是连接错误，模拟一个响应
          if (aiResponse.content === '') {
            aiResponse.content = '抱歉，连接失败，请稍后重试。（SSE模拟响应）'
          }
          this.updateChatHistory(aiResponse.content, true)
          this.eventSource.close()
        }
      } catch (error) {
        console.error('SSE调用失败:', error)
        this.isTyping = false
        aiResponse.content = '抱歉，服务暂时不可用，请稍后重试。（模拟响应）'
        this.updateChatHistory(aiResponse.content, true)
      }
    },
    
    // 更新聊天历史
    updateChatHistory(message, isAI = false) {
      const currentChat = this.chatHistory.find(chat => chat.id === this.currentChatId)
      if (currentChat) {
        currentChat.lastMessage = message.length > 30 ? message.substring(0, 30) + '...' : message
        if (isAI && !currentChat.title) {
          // 如果是AI回复且没有标题，设置标题
          currentChat.title = message.length > 20 ? message.substring(0, 20) + '...' : message
        }
      }
    },
    
    // 滚动到底部
    scrollToBottom() {
      if (this.$refs.messagesContainer) {
        this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
.cupid-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 左侧边栏 */
.sidebar {
  width: 300px;
  background-color: #f5f5f5;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.new-chat-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background-color: #667eea;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
}

.new-chat-btn:hover {
  background-color: #764ba2;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  padding: 15px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #e0e0e0;
}

.chat-item:hover {
  background-color: #e8e8e8;
}

.chat-item.active {
  background-color: #667eea;
  color: white;
}

.chat-preview {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.chat-title {
  font-weight: 500;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-subtitle {
  font-size: 0.85rem;
  opacity: 0.7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 聊天区域 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  color: white;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.3rem;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 1.1rem;
}

.message {
  display: flex;
  margin-bottom: 15px;
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.5;
}

.message.user .message-content {
  background-color: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.ai .message-content {
  background-color: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}

/* 输入区域 */
.chat-input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.chat-input-area textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  resize: none;
  font-size: 1rem;
  line-height: 1.5;
  max-height: 150px;
}

.chat-input-area textarea:focus {
  outline: none;
  border-color: #667eea;
}

.send-btn {
  padding: 12px 24px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
  align-self: flex-end;
}

.send-btn:hover:not(:disabled) {
  background-color: #764ba2;
}

.send-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 5px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background-color: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 250px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-input-area {
    padding: 15px;
  }
}

/* 移动端适配 */
@media (max-width: 600px) {
  .cupid-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: 60px;
    position: fixed;
    bottom: 0;
    z-index: 100;
  }
  
  .chat-history {
    display: none;
  }
  
  .chat-container {
    height: calc(100vh - 60px);
  }
}
</style>