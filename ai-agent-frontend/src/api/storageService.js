/**
 * 本地存储服务
 * 用于管理聊天历史、会话信息等本地数据
 */
const STORAGE_KEYS = {
  CHAT_HISTORY: 'ai_agent_chat_history',
  CURRENT_CHAT_ID: 'ai_agent_current_chat_id',
  TASK_HISTORY: 'ai_agent_task_history',
  USER_PREFERENCES: 'ai_agent_user_preferences'
};

class StorageService {
  /**
   * 保存数据到本地存储
   * @param {string} key - 存储键
   * @param {*} data - 要存储的数据
   * @returns {boolean} - 是否保存成功
   */
  setItem(key, data) {
    try {
      const serializedData = JSON.stringify(data);
      localStorage.setItem(key, serializedData);
      return true;
    } catch (error) {
      console.error(`保存数据失败 (${key}):`, error);
      return false;
    }
  }

  /**
   * 从本地存储获取数据
   * @param {string} key - 存储键
   * @param {*} defaultValue - 默认值
   * @returns {*} - 存储的数据或默认值
   */
  getItem(key, defaultValue = null) {
    try {
      const serializedData = localStorage.getItem(key);
      if (serializedData === null) {
        return defaultValue;
      }
      return JSON.parse(serializedData);
    } catch (error) {
      console.error(`读取数据失败 (${key}):`, error);
      return defaultValue;
    }
  }

  /**
   * 从本地存储删除数据
   * @param {string} key - 存储键
   * @returns {boolean} - 是否删除成功
   */
  removeItem(key) {
    try {
      localStorage.removeItem(key);
      return true;
    } catch (error) {
      console.error(`删除数据失败 (${key}):`, error);
      return false;
    }
  }

  /**
   * 清空所有本地存储数据
   * @returns {boolean} - 是否清空成功
   */
  clear() {
    try {
      localStorage.clear();
      return true;
    } catch (error) {
      console.error('清空存储失败:', error);
      return false;
    }
  }

  /**
   * 保存聊天历史
   * @param {Array} chatHistory - 聊天历史数组
   * @returns {boolean} - 是否保存成功
   */
  saveChatHistory(chatHistory) {
    return this.setItem(STORAGE_KEYS.CHAT_HISTORY, chatHistory);
  }

  /**
   * 获取聊天历史
   * @returns {Array} - 聊天历史数组
   */
  getChatHistory() {
    return this.getItem(STORAGE_KEYS.CHAT_HISTORY, []);
  }

  /**
   * 保存当前会话ID
   * @param {string} chatId - 会话ID
   * @returns {boolean} - 是否保存成功
   */
  saveCurrentChatId(chatId) {
    return this.setItem(STORAGE_KEYS.CURRENT_CHAT_ID, chatId);
  }

  /**
   * 获取当前会话ID
   * @returns {string|null} - 当前会话ID
   */
  getCurrentChatId() {
    return this.getItem(STORAGE_KEYS.CURRENT_CHAT_ID, null);
  }

  /**
   * 保存任务历史（AI超级智能体）
   * @param {Array} taskHistory - 任务历史数组
   * @returns {boolean} - 是否保存成功
   */
  saveTaskHistory(taskHistory) {
    return this.setItem(STORAGE_KEYS.TASK_HISTORY, taskHistory);
  }

  /**
   * 获取任务历史
   * @returns {Array} - 任务历史数组
   */
  getTaskHistory() {
    return this.getItem(STORAGE_KEYS.TASK_HISTORY, []);
  }

  /**
   * 保存用户偏好设置
   * @param {Object} preferences - 用户偏好设置
   * @returns {boolean} - 是否保存成功
   */
  saveUserPreferences(preferences) {
    return this.setItem(STORAGE_KEYS.USER_PREFERENCES, preferences);
  }

  /**
   * 获取用户偏好设置
   * @returns {Object} - 用户偏好设置
   */
  getUserPreferences() {
    return this.getItem(STORAGE_KEYS.USER_PREFERENCES, {
      theme: 'light',
      fontSize: 14,
      enableSound: false
    });
  }

  /**
   * 检查本地存储是否可用
   * @returns {boolean} - 是否可用
   */
  isAvailable() {
    try {
      const testKey = '__localStorage_test__';
      localStorage.setItem(testKey, testKey);
      localStorage.removeItem(testKey);
      return true;
    } catch (e) {
      return false;
    }
  }

  /**
   * 获取存储使用情况
   * @returns {Object} - 存储使用情况
   */
  getStorageInfo() {
    if (!this.isAvailable()) {
      return { available: false };
    }

    let totalSize = 0;
    const storageItems = {};

    Object.values(STORAGE_KEYS).forEach(key => {
      const item = localStorage.getItem(key);
      if (item) {
        const size = new Blob([item]).size;
        totalSize += size;
        storageItems[key] = {
          size,
          sizeFormatted: this.formatBytes(size)
        };
      }
    });

    return {
      available: true,
      totalSize,
      totalSizeFormatted: this.formatBytes(totalSize),
      items: storageItems
    };
  }

  /**
   * 格式化字节大小
   * @param {number} bytes - 字节数
   * @returns {string} - 格式化后的大小
   */
  formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  /**
   * 清理过期数据
   * @param {number} maxAge - 最大保留时间（毫秒）
   */
  cleanupExpiredData(maxAge = 7 * 24 * 60 * 60 * 1000) { // 默认7天
    const now = Date.now();
    
    // 清理聊天历史
    const chatHistory = this.getChatHistory();
    const filteredChats = chatHistory.filter(chat => {
      return !chat.timestamp || (now - chat.timestamp) < maxAge;
    });
    this.saveChatHistory(filteredChats);
    
    // 清理任务历史
    const taskHistory = this.getTaskHistory();
    const filteredTasks = taskHistory.filter(task => {
      return !task.timestamp || (now - task.timestamp) < maxAge;
    });
    this.saveTaskHistory(filteredTasks);
  }
}

// 导出单例实例
const storageService = new StorageService();

// 自动清理过期数据
storageService.cleanupExpiredData();

export { STORAGE_KEYS };
export default storageService;