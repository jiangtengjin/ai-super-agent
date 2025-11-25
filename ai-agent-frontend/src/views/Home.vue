<template>
  <div class="home-container">
    <div class="home-content">
      <h1 class="home-title">AI Agent 应用中心</h1>
      <p class="home-subtitle">选择您想要使用的 AI 应用</p>
      
      <div class="app-grid">
        <div 
          class="app-card" 
          @click="goToApp('/love-master')"
        >
          <div class="app-icon">💕</div>
          <h2 class="app-title">AI 恋爱大师</h2>
          <p class="app-description">专业的恋爱咨询助手，为您提供情感建议和指导</p>
        </div>
        
        <div 
          class="app-card" 
          @click="goToApp('/super-agent')"
        >
          <div class="app-icon">🤖</div>
          <h2 class="app-title">AI 超级智能体</h2>
          <p class="app-description">具备思考、行动和工具调用能力的智能助手</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { createConversationId } from '@/api/conversation'
import { message } from 'ant-design-vue'

const router = useRouter()

const goToApp = async (path: string) => {
  if (path === '/love-master') {
    try {
      const response = await createConversationId()
      const conversationId = response.data.data
      if (!conversationId) {
        throw new Error('未获取到 conversationId')
      }
      router.push({ name: 'LoveMaster', params: { conversationId } })
    } catch (error) {
      console.error('无法创建对话', error)
      message.error('创建对话失败，请稍后再试')
    }
    return
  }
  router.push(path)
}
</script>

<style scoped>
.home-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.home-content {
  max-width: 1200px;
  width: 100%;
  text-align: center;
}

.home-title {
  font-size: 3rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.home-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 3rem;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.app-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 2.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.app-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.app-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.75rem;
}

.app-description {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .home-title {
    font-size: 2rem;
  }
  
  .home-subtitle {
    font-size: 1rem;
  }
  
  .app-grid {
    grid-template-columns: 1fr;
  }
}
</style>

