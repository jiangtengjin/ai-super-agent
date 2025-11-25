import api from './index'
import type { ApiResponse } from './types'

export interface ConversationRecord {
  id?: number
  name?: string
  conversationId: string
  createTime?: string
  updateTime?: string
}

export interface ConversationPage {
  records: ConversationRecord[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ChatHistoryRecord {
  id: number
  message: string
  conversationId: string
  messageType: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'
  createTime?: string
}

export function fetchConversationList(params: { pageSize?: number; lastCreateTime?: string } = {}) {
  return api.get<ApiResponse<ConversationPage>>('/conversation/page', { params })
}

export function fetchConversationHistory(conversationId: string) {
  return api.get<ApiResponse<ChatHistoryRecord[]>>(`/conversation/${conversationId}/history`)
}

export function createConversationId() {
  return api.get<ApiResponse<string>>('/conversation/get/conversationId')
}

