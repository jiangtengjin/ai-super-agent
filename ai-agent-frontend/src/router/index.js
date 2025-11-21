import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/cupid',
      name: 'cupid',
      component: () => import('../views/AICupid.vue')
    },
    {
      path: '/agent',
      name: 'agent',
      component: () => import('../views/AIAgent.vue')
    }
  ]
})

export default router