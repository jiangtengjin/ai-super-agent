import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import LoveMaster from '@/views/LoveMaster.vue'
import SuperAgent from '@/views/SuperAgent.vue'
import { createConversationId } from '@/api/conversation'

const ensureConversationId = async (to, from, next) => {
  if (typeof to.params.conversationId === 'string' && to.params.conversationId) {
    return next()
  }
  try {
    const response = await createConversationId()
    const conversationId = response.data.data
    if (!conversationId) {
      throw new Error('获取 conversationId 失败')
    }
    next({
      name: to.name || 'LoveMaster',
      params: {
        ...to.params,
        conversationId
      },
      query: to.query,
      replace: true
    })
  } catch (error) {
    console.error('Failed to create conversation id', error)
    next(false)
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/love-master/:conversationId?',
      name: 'LoveMaster',
      component: LoveMaster,
      beforeEnter: ensureConversationId
    },
    {
      path: '/super-agent',
      name: 'SuperAgent',
      component: SuperAgent
    }
  ]
})

export default router

