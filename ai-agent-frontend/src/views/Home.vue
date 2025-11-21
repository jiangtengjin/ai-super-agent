<template>
  <div class="home-container">
    <h1 class="title">AI 应用中心</h1>
    <div class="app-cards">
      <router-link to="/cupid" class="app-card cupid-card">
        <div class="app-icon">💘</div>
        <h2 class="app-title">AI 恋爱大师</h2>
        <p class="app-desc">专业的恋爱咨询和情感分析助手</p>
      </router-link>
      
      <router-link to="/agent" class="app-card agent-card">
        <div class="app-icon">🤖</div>
        <h2 class="app-title">AI 超级智能体</h2>
        <p class="app-desc">强大的智能代理，支持思考和工具调用</p>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-cards {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.app-card {
  width: 300px;
  height: 250px;
  border-radius: 20px;
  padding: 30px;
  text-decoration: none;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.cupid-card {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  color: #fff;
}

.agent-card {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #333;
}

.app-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.app-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.app-desc {
  font-size: 1rem;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .title {
    font-size: 2rem;
  }
  
  .app-card {
    width: 280px;
    height: 220px;
  }
  
  .app-icon {
    font-size: 3rem;
  }
  
  .app-title {
    font-size: 1.3rem;
  }
}
</style>