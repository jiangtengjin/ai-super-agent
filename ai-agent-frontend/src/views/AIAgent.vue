<template>
  <div class="agent-container">
    <div class="agent-header">
      <h1 class="title">AI 超级智能体 🤖</h1>
      <p class="subtitle">强大的智能代理，支持思考和工具调用</p>
    </div>
    
    <div class="agent-content">
      <!-- 输入区域 -->
      <div class="input-section">
        <textarea
          v-model="userPrompt"
          @keyup.enter.ctrl="sendPrompt"
          @keyup.enter.meta="sendPrompt"
          placeholder="输入您的任务指令，按 Ctrl+Enter 发送..."
          :disabled="isProcessing"
          ref="promptTextarea"
        ></textarea>
        <div class="input-actions">
          <button 
            @click="clearHistory" 
            class="clear-btn"
            :disabled="isProcessing"
          >清空历史</button>
          <button 
            @click="sendPrompt" 
            class="submit-btn"
            :disabled="isProcessing || !userPrompt.trim()"
          >
            {{ isProcessing ? '处理中...' : '开始任务' }}
          </button>
        </div>
      </div>
      
      <!-- 任务列表 --</div
      <div class="task-history">
        <div v-if="taskList.length === 0" class="empty-state">
          <p>输入任务指令开始使用AI超级智能体</p>
        </div>
        <div 
          v-for="(task, index) in taskList" 
          :key="task.id"
          class="task-item"
        >
          <div class="task-header">
            <div class="task-title">
              <span class="task-index">任务 {{ index + 1 }}</span>
              <span class="task-status" :class="task.status">{{ getStatusText(task.status) }}</span>
            </div>
            <div class="task-time">{{ formatTime(task.timestamp) }}</div>
          </div>
          <div class="task-content">
            <div class="user-prompt">
              <span class="role-label user">用户</span>
              <div class="content-text">{{ task.prompt }}</div>
            </div>
            <div v-if="task.steps.length > 0" class="agent-steps">
              <div 
                v-for="(step, stepIndex) in task.steps" 
                :key="stepIndex"
                :class="['step-item', `step-${step.type}`]"
              >
                <div class="step-header">
                  <span class="step-type">{{ getStepTypeText(step.type) }}</span>
                </div>
                <div class="step-content">
                  <pre v-if="step.type === 'tool_call'">{{ formatToolCall(step.content) }}</pre>
                  <div v-else>{{ step.content }}</div>
                <div v-if="step.result && step.type === 'tool_call'" class="tool-result">
                  <div class="result-header">工具执行结果</div>
                  <pre>{{ step.result }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AIAgent',
  data() {
    return {
      userPrompt: '',
      isProcessing: false,
      taskList: [],
      currentTaskId: null,
      eventSource: null
    }
  },
  beforeUnmount() {
    // 关闭SSE连接
    if (this.eventSource) {
      this.eventSource.close()
    }
  },
  methods: {
    // 发送任务指令
    sendPrompt() {
      if (!this.userPrompt.trim() || this.isProcessing) return
      
      const prompt = this.userPrompt.trim()
      const taskId = 'task-' + Date.now()
      this.currentTaskId = taskId
      
      // 创建新任务
      const newTask = {
        id: taskId,
        prompt: prompt,
        timestamp: new Date(),
        status: 'processing',
        steps: []
      }
      
      this.taskList.unshift(newTask)
      this.userPrompt = ''
      this.isProcessing = true
      
      // 滚动到最新任务
      this.$nextTick(() => this.scrollToNewTask())
      
      // 调用后端接口
      this.callAgentAPI(prompt, taskId)
    },
    
    // 调用AI智能体接口
    callAgentAPI(prompt, taskId) {
      // 如果有旧的连接，先关闭
      if (this.eventSource) {
        this.eventSource.close()
      }
      
      // 构建请求参数
      const params = new URLSearchParams()
      params.append('prompt', prompt)
      
      try {
        // 创建EventSource连接
        this.eventSource = new EventSource(`http://localhost:8123/api/ai/doChatWithManus?${params.toString()}`)
        
        // 处理消息事件
        this.eventSource.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data)
            this.processAgentEvent(data, taskId)
          } catch (e) {
            // 如果不是JSON格式，作为普通文本处理
            this.addStep(taskId, 'text', event.data)
          }
        }
        
        // 处理连接结束
        this.eventSource.onclose = () => {
          this.isProcessing = false
          this.updateTaskStatus(taskId, 'completed')
        }
        
        // 处理错误
        this.eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          this.isProcessing = false
          this.updateTaskStatus(taskId, 'error')
          this.addStep(taskId, 'error', '处理过程中发生错误，请稍后重试。')
          this.eventSource.close()
        }
      } catch (error) {
        console.error('调用失败:', error)
        this.isProcessing = false
        this.updateTaskStatus(taskId, 'error')
        this.addStep(taskId, 'error', '服务暂时不可用，请稍后重试。')
      }
    },
    
    // 处理代理事件
    processAgentEvent(data, taskId) {
      if (data.type === 'thoughts') {
        this.addStep(taskId, 'thoughts', data.content)
      } else if (data.type === 'tool_selection') {
        this.addStep(taskId, 'tool_selection', data.content)
      } else if (data.type === 'tool_call') {
        this.addStep(taskId, 'tool_call', data)
      } else if (data.type === 'tool_result') {
        this.updateToolResult(taskId, data.content)
      } else if (data.type === 'conclusion') {
        this.addStep(taskId, 'conclusion', data.content)
      } else if (data.type === 'error') {
        this.addStep(taskId, 'error', data.content)
        this.updateTaskStatus(taskId, 'error')
      }
    },
    
    // 添加步骤
    addStep(taskId, type, content) {
      const task = this.taskList.find(t => t.id === taskId)
      if (task) {
        task.steps.push({
          type: type,
          content: content,
          result: null
        })
        this.$nextTick(() => this.scrollToNewStep(taskId))
      }
    },
    
    // 更新工具执行结果
    updateToolResult(taskId, result) {
      const task = this.taskList.find(t => t.id === taskId)
      if (task) {
        const lastStep = task.steps[task.steps.length - 1]
        if (lastStep && lastStep.type === 'tool_call') {
          lastStep.result = result
        }
      }
    },
    
    // 更新任务状态
    updateTaskStatus(taskId, status) {
      const task = this.taskList.find(t => t.id === taskId)
      if (task) {
        task.status = status
      }
    },
    
    // 格式化工具调用信息
    formatToolCall(toolCall) {
      if (typeof toolCall === 'object') {
        return JSON.stringify(toolCall, null, 2)
      }
      return toolCall
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        processing: '处理中',
        completed: '已完成',
        error: '出错'
      }
      return statusMap[status] || status
    },
    
    // 获取步骤类型文本
    getStepTypeText(type) {
      const typeMap = {
        thoughts: '🤔 思考',
        tool_selection: '🔧 工具选择',
        tool_call: '🚀 工具调用',
        conclusion: '📝 总结',
        text: '💬 文本',
        error: '❌ 错误'
      }
      return typeMap[type] || type
    },
    
    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    
    // 清空历史
    clearHistory() {
      if (confirm('确定要清空所有历史记录吗？')) {
        this.taskList = []
      }
    },
    
    // 滚动到最新任务
    scrollToNewTask() {
      this.$nextTick(() => {
        const taskElements = document.querySelectorAll('.task-item')
        if (taskElements.length > 0) {
          taskElements[0].scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
    },
    
    // 滚动到最新步骤
    scrollToNewStep(taskId) {
      this.$nextTick(() => {
        const taskElement = document.querySelector(`[data-task-id="${taskId}"]`)
        if (taskElement) {
          const steps = taskElement.querySelectorAll('.step-item')
          if (steps.length > 0) {
            steps[steps.length - 1].scrollIntoView({ behavior: 'smooth', block: 'end' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.agent-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background-color: #fafafa;
}

.agent-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.agent-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 输入区域 */
.input-section {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.input-section textarea {
  width: 100%;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  resize: vertical;
  min-height: 100px;
  font-size: 1rem;
  line-height: 1.5;
  margin-bottom: 15px;
  transition: border-color 0.3s;
}

.input-section textarea:focus {
  outline: none;
  border-color: #a8edea;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.clear-btn {
  padding: 10px 20px;
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-btn:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.submit-btn {
  padding: 10px 24px;
  background-color: #a8edea;
  color: #333;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #95d9d6;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 任务历史 */
.task-history {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.task-item {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.task-item:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.task-header {
  padding: 15px 20px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-index {
  font-weight: 600;
  color: #333;
}

.task-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.task-status.processing {
  background-color: #e3f2fd;
  color: #1976d2;
}

.task-status.completed {
  background-color: #e8f5e9;
  color: #388e3c;
}

.task-status.error {
  background-color: #ffebee;
  color: #d32f2f;
}

.task-time {
  font-size: 0.85rem;
  color: #666;
}

.task-content {
  padding: 20px;
}

.user-prompt {
  margin-bottom: 20px;
}

.role-label {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
  margin-bottom: 10px;
}

.role-label.user {
  background-color: #667eea;
  color: white;
}

.role-label.agent {
  background-color: #a8edea;
  color: #333;
}

.content-text {
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 8px;
  line-height: 1.6;
}

/* 步骤样式 */
.agent-steps {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.step-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.step-header {
  padding: 10px 15px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
}

.step-type {
  font-weight: 500;
  font-size: 0.9rem;
}

.step-content {
  padding: 15px;
  line-height: 1.6;
}

.step-thoughts .step-header {
  background-color: #e3f2fd;
}

.step-tool_selection .step-header {
  background-color: #e8f5e9;
}

.step-tool_call .step-header {
  background-color: #fff3e0;
}

.step-conclusion .step-header {
  background-color: #f3e5f5;
}

.step-error .step-header {
  background-color: #ffebee;
}

/* 工具调用和结果样式 */
.step-content pre {
  margin: 0;
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.tool-result {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.result-header {
  font-weight: 500;
  margin-bottom: 10px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .agent-container {
    padding: 15px;
  }
  
  .title {
    font-size: 2rem;
  }
  
  .input-actions {
    flex-direction: column;
  }
  
  .clear-btn,
  .submit-btn {
    width: 100%;
  }
  
  .task-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .task-content {
    padding: 15px;
  }
}
</style>