import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 180000, // 3分钟超时，匹配后端 SSE 超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

export default api

