<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { 
  listAllChatHistoryByPageForAdmin
} from '@/api/chatHistoryController'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 对话列表
const chats = ref<API.ChatHistory[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索条件
const searchForm = ref({
  message: '',
  messageType: '',
  appId: '',
  userId: '',
  lastCreateTime: ''
})

// 计算属性
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 初始化
onMounted(() => {
  if (!isAdmin.value) {
    alert('您没有权限访问此页面')
    router.push('/')
    return
  }
  loadChats()
})

// 加载对话列表
const loadChats = async () => {
  loading.value = true
  try {
    const response = await listAllChatHistoryByPageForAdmin({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      message: searchForm.value.message || undefined,
      messageType: searchForm.value.messageType || undefined,
      appId: searchForm.value.appId || undefined,
      userId: searchForm.value.userId || undefined,
      lastCreateTime: searchForm.value.lastCreateTime || undefined
    })
    
    if (response.data.code === 0 && response.data.data) {
      chats.value = response.data.data.records || []
      total.value = response.data.data.totalRow || 0
    } else {
      throw new Error(response.data.message || '加载对话列表失败')
    }
  } catch (error) {
    console.error('加载对话列表失败:', error)
    alert('加载对话列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadChats()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    message: '',
    messageType: '',
    appId: '',
    userId: '',
    lastCreateTime: ''
  }
  currentPage.value = 1
  loadChats()
}

// 查看应用详情
const viewApp = (chat: API.ChatHistory) => {
  if (chat.appId) {
    router.push(`/app/chat/${chat.appId}`)
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

// 格式化消息内容
const formatMessage = (message: string) => {
  if (!message) return '-'
  return message.length > 50 ? message.substring(0, 50) + '...' : message
}

// 获取消息类型显示文本
const getMessageTypeText = (type: string) => {
  switch (type) {
    case 'user':
      return '用户消息'
    case 'ai':
      return 'AI回复'
    default:
      return type || '-'
  }
}
</script>

<template>
  <div class="chat-manage-page">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h1>对话管理</h1>
          <p>管理所有用户的对话记录</p>
        </div>
        <button @click="router.push('/')" class="back-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12,19 5,12 12,5"></polyline>
          </svg>
          返回首页
        </button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-form">
        <div class="form-item">
          <label>消息内容:</label>
          <input 
            v-model="searchForm.message" 
            placeholder="搜索消息内容"
            class="search-input"
          />
        </div>
        <div class="form-item">
          <label>消息类型:</label>
          <select v-model="searchForm.messageType" class="search-input">
            <option value="">全部类型</option>
            <option value="user">用户消息</option>
            <option value="ai">AI回复</option>
          </select>
        </div>
        <div class="form-item">
          <label>应用ID:</label>
          <input 
            v-model="searchForm.appId" 
            placeholder="搜索应用ID"
            class="search-input"
          />
        </div>
        <div class="form-item">
          <label>用户ID:</label>
          <input 
            v-model="searchForm.userId" 
            placeholder="搜索用户ID"
            class="search-input"
          />
        </div>
        <div class="form-item">
          <label>创建时间:</label>
          <input 
            v-model="searchForm.lastCreateTime" 
            type="datetime-local"
            class="search-input"
          />
        </div>
        <div class="form-actions">
          <button @click="handleSearch" class="search-btn">搜索</button>
          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>
    </div>

    <!-- 对话列表 -->
    <div class="chats-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>消息内容</th>
            <th>消息类型</th>
            <th>应用ID</th>
            <th>用户ID</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="chat in chats" :key="chat.id">
            <td>{{ chat.id }}</td>
            <td>
              <div class="message-content">
                <span class="message-text">{{ formatMessage(chat.message || '') }}</span>
              </div>
            </td>
            <td>
              <span :class="['message-type', chat.messageType === 'user' ? 'user' : 'ai']">
                {{ getMessageTypeText(chat.messageType || '') }}
              </span>
            </td>
            <td>{{ chat.appId || '-' }}</td>
            <td>{{ chat.userId || '-' }}</td>
            <td>{{ formatTime(chat.createTime || '') }}</td>
            <td>
              <div class="action-buttons">
                <button @click="viewApp(chat)" class="action-btn view-btn">
                  查看应用
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && chats.length === 0" class="empty-state">
        <p>暂无对话数据</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <button 
        @click="currentPage--; loadChats()"
        :disabled="currentPage <= 1"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">
        {{ currentPage }} / {{ Math.ceil(total / pageSize) }}
      </span>
      <button 
        @click="currentPage++; loadChats()"
        :disabled="currentPage >= Math.ceil(total / pageSize)"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-manage-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f5f5f5;
  border-color: #1890ff;
  color: #1890ff;
}

.back-btn svg {
  width: 16px;
  height: 16px;
}

.search-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  align-items: end;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #1890ff;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.search-btn, .reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.search-btn {
  background: #1890ff;
  color: white;
}

.search-btn:hover {
  background: #40a9ff;
}

.reset-btn {
  background: #f5f5f5;
  color: #666;
}

.reset-btn:hover {
  background: #e8e8e8;
}

.chats-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

.message-content {
  max-width: 300px;
}

.message-text {
  font-size: 14px;
  color: #333;
  word-break: break-word;
}

.message-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.message-type.user {
  background: #e6f7ff;
  color: #1890ff;
}

.message-type.ai {
  background: #f6ffed;
  color: #52c41a;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.view-btn {
  background: #1890ff;
  color: white;
}

.view-btn:hover {
  background: #40a9ff;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .search-form {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .message-content {
    max-width: 200px;
  }
}
</style> 