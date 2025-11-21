import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 可以在这里添加认证token等
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    // 统一错误处理
    console.error('API请求错误:', error);
    
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response;
      console.error(`请求失败: ${status}`, data);
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('网络错误，请检查您的网络连接');
    } else {
      // 请求配置出错
      console.error('请求配置错误:', error.message);
    }
    
    return Promise.reject(error);
  }
);

/**
 * SSE连接管理类
 */
class SSEManager {
  constructor() {
    this.eventSource = null;
    this.listeners = new Map();
  }

  /**
   * 建立SSE连接
   * @param {string} url - SSE接口URL
   * @param {Object} options - 配置选项
   * @returns {EventSource} - EventSource实例
   */
  connect(url, options = {}) {
    // 关闭现有连接
    if (this.eventSource) {
      this.close();
    }

    // 创建新的EventSource连接
    this.eventSource = new EventSource(url, options);

    // 注册基础事件监听器
    this.eventSource.onopen = (event) => {
      this.notifyListeners('open', event);
    };

    this.eventSource.onmessage = (event) => {
      try {
        // 尝试解析JSON数据
        const data = JSON.parse(event.data);
        this.notifyListeners('message', data);
      } catch (error) {
        // 如果解析失败，直接传递原始数据
        this.notifyListeners('message', event.data);
      }
    };

    this.eventSource.onerror = (error) => {
      this.notifyListeners('error', error);
    };

    this.eventSource.onclose = () => {
      this.notifyListeners('close');
    };

    return this.eventSource;
  }

  /**
   * 添加事件监听器
   * @param {string} eventType - 事件类型
   * @param {Function} callback - 回调函数
   */
  on(eventType, callback) {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, []);
    }
    this.listeners.get(eventType).push(callback);
  }

  /**
   * 移除事件监听器
   * @param {string} eventType - 事件类型
   * @param {Function} callback - 回调函数（可选，不提供则移除该类型所有监听器）
   */
  off(eventType, callback) {
    if (this.listeners.has(eventType)) {
      if (callback) {
        const callbacks = this.listeners.get(eventType);
        const index = callbacks.indexOf(callback);
        if (index !== -1) {
          callbacks.splice(index, 1);
        }
      } else {
        this.listeners.delete(eventType);
      }
    }
  }

  /**
   * 通知所有监听器
   * @param {string} eventType - 事件类型
   * @param {*} data - 事件数据
   */
  notifyListeners(eventType, data) {
    if (this.listeners.has(eventType)) {
      this.listeners.get(eventType).forEach(callback => {
        try {
          callback(data);
        } catch (error) {
          console.error(`SSE监听器错误 (${eventType}):`, error);
        }
      });
    }
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      this.listeners.clear();
    }
  }

  /**
   * 检查连接是否活跃
   * @returns {boolean} - 是否已连接
   */
  isConnected() {
    return this.eventSource !== null && this.eventSource.readyState === 0;
  }
}

// API接口方法
const apiService = {
  // 常规API请求方法
  get: (url, params) => apiClient.get(url, { params }),
  post: (url, data) => apiClient.post(url, data),
  put: (url, data) => apiClient.put(url, data),
  delete: (url) => apiClient.delete(url),

  // SSE相关方法
  createSSE: () => new SSEManager(),

  /**
   * AI恋爱大师SSE聊天接口
   * @param {string} prompt - 用户输入
   * @param {string} chatId - 会话ID
   * @param {Object} callbacks - 事件回调对象
   * @returns {SSEManager} - SSE管理器实例
   */
  chatWithSSE: (prompt, chatId, callbacks = {}) => {
    const sseManager = new SSEManager();
    
    // 构建请求URL
    const params = new URLSearchParams({
      prompt,
      chatId
    });
    
    // 连接SSE
    sseManager.connect(`http://localhost:8123/api/ai/doChatWithSseEmitter?${params.toString()}`);
    
    // 注册回调
    Object.entries(callbacks).forEach(([event, callback]) => {
      sseManager.on(event, callback);
    });
    
    return sseManager;
  },

  /**
   * AI超级智能体接口
   * @param {string} prompt - 用户指令
   * @param {Object} callbacks - 事件回调对象
   * @returns {SSEManager} - SSE管理器实例
   */
  chatWithAgent: (prompt, callbacks = {}) => {
    const sseManager = new SSEManager();
    
    // 构建请求URL
    const params = new URLSearchParams({
      prompt
    });
    
    // 连接SSE
    sseManager.connect(`http://localhost:8123/api/ai/doChatWithManus?${params.toString()}`);
    
    // 注册回调
    Object.entries(callbacks).forEach(([event, callback]) => {
      sseManager.on(event, callback);
    });
    
    return sseManager;
  },

  /**
   * 生成唯一的会话ID
   * @returns {string} - 唯一ID
   */
  generateChatId: () => {
    return 'chat_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9);
  },

  /**
   * 取消请求
   * @param {Object} source - 请求源
   */
  cancelRequest: (source) => {
    if (source && source.cancel) {
      source.cancel('Request canceled by user');
    }
  },

  /**
   * 创建可取消的请求源
   * @returns {Object} - 请求源
   */
  createCancelToken: () => {
    return axios.CancelToken.source();
  }
};

export default apiService;