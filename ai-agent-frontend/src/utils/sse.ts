/**
 * SSE 事件源处理工具
 */
export interface SSEMessage {
  type: 'data' | 'error' | 'complete'
  data?: string
  error?: Error
}

export class SSEManager {
  private eventSource: EventSource | null = null
  private onMessageCallback?: (data: string) => void
  private onErrorCallback?: (error: Error) => void
  private onCompleteCallback?: () => void

  /**
   * 创建 SSE 连接
   */
  connect(eventSource: EventSource, eventName = 'message') {
    this.eventSource = eventSource

    this.eventSource.addEventListener(eventName, (event: Event) => {
      const messageEvent = event as MessageEvent
      if (this.onMessageCallback) {
        this.onMessageCallback(messageEvent.data)
      }
    })

    this.eventSource.addEventListener('error', () => {
      if (this.onErrorCallback) {
        this.onErrorCallback(new Error('SSE connection error'))
      }
      this.close()
    })

    // 监听自定义事件（如果有）
    this.eventSource.addEventListener('complete', () => {
      if (this.onCompleteCallback) {
        this.onCompleteCallback()
      }
      this.close()
    })
  }

  /**
   * 设置消息回调
   */
  onMessage(callback: (data: string) => void) {
    this.onMessageCallback = callback
  }

  /**
   * 设置错误回调
   */
  onError(callback: (error: Error) => void) {
    this.onErrorCallback = callback
  }

  /**
   * 设置完成回调
   */
  onComplete(callback: () => void) {
    this.onCompleteCallback = callback
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
  }

  /**
   * 检查连接状态
   */
  isConnected(): boolean {
    return this.eventSource !== null && this.eventSource.readyState === EventSource.OPEN
  }
}

