<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById, chatToGenCode, deployApp } from '@/api/appController'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO | null>(null)
const appLoading = ref(true)

// 对话相关
const messages = ref<Array<{
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: Date
}>>([])
const userInput = ref('')
const isGenerating = ref(false)
const currentStreamContent = ref('')

// 部署相关
const isDeploying = ref(false)
const deployedUrl = ref('')
const showDeploySuccessModal = ref(false)

// 计算属性
const appId = computed(() => {
  const id = route.params.appId
  return String(id) // 使用字符串类型，因为后端ID是字符串
})

// 检查用户登录状态
const isLoggedIn = computed(() => !!loginUserStore.loginUser.userAccount)

// 检查是否有编辑权限 - 只要登录就可以编辑
const canEdit = computed(() => {
  return isLoggedIn.value
})

const loadAppInfo = async () => {
  try {
    appLoading.value = true
    console.log('开始加载应用信息，appId:', appId.value)
    
    // 测试网络连接 - 使用更简单的端点
    try {
      const testResponse = await fetch('http://localhost:8123/api/app/get/vo?id=308440364145102848', {
        method: 'GET',
        credentials: 'include'
      })
      console.log('后端服务连接测试:', testResponse.status)
      if (!testResponse.ok) {
        throw new Error(`HTTP ${testResponse.status}`)
      }
    } catch (testError) {
      console.error('后端服务连接失败:', testError)
      alert('后端服务连接失败，请检查服务是否运行')
      return
    }
    
    const response = await getAppVoById({ id: appId.value })
    console.log('API响应:', response)
    console.log('API响应类型:', typeof response)
    console.log('API响应字段:', Object.keys(response))
    
    // 检查响应结构
    if (response && typeof response === 'object') {
      // 检查是否有 data 字段
      if (response.data && response.data.code === 0) {
        appInfo.value = response.data.data
        console.log('应用信息加载成功:', appInfo.value?.appName)
      } else if (response.code === 0) {
        appInfo.value = response.data
        console.log('应用信息加载成功:', appInfo.value?.appName)
      } else {
        console.error('API返回错误:', response)
        throw new Error(response.message || '加载应用信息失败')
      }
    } else {
      console.error('API响应格式异常:', response)
      throw new Error('API响应格式异常')
    }
  } catch (error) {
    console.error('加载应用信息失败:', error)
    console.error('错误详情:', {
      message: error.message,
      stack: error.stack,
      appId: appId.value
    })
    alert('加载应用信息失败')
    router.push('/')
  } finally {
    appLoading.value = false
  }
}

// 初始化
onMounted(async () => {
  // 确保用户已登录
  if (!loginUserStore.loginUser.userAccount) {
    await loginUserStore.fetchLoginUser()
  }
  
  await loadAppInfo()
})

// 发送消息
const sendMessage = async (content: string) => {
  if (!content || !content.trim()) {
    return
  }
  
  // 移除权限检查，允许发送消息
  // if (!canEdit.value) {
  //   alert('您没有权限修改此作品，只有作品所有者才能进行编辑。')
  //   return
  // }
  
  const userMessage = {
    id: Date.now().toString(),
    type: 'user' as const,
    content: content.trim(),
    timestamp: new Date()
  }
  
  messages.value.push(userMessage)
  
  // 用户发送新消息时滚动到底部
  nextTick().then(() => {
    scrollToBottom();
  });
  
  isGenerating.value = true
  currentStreamContent.value = ''
  
  try {
    const aiMessage = {
      id: (Date.now() + 1).toString(),
      type: 'ai' as const,
      content: '',
      timestamp: new Date()
    }
    
    messages.value.push(aiMessage)
    
    // 手动创建EventSource来实现实时流式更新
    const queryString = new URLSearchParams({ 
      appId: appId.value, 
      message: content.trim() 
    }).toString();
    const url = `http://localhost:8123/api/app/chat/gen/code?${queryString}`;
    
    const eventSource = new EventSource(url, { withCredentials: true });
    
    eventSource.onopen = () => {
      console.log('EventSource连接已建立')
    }
    
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.d) {
          currentStreamContent.value += data.d;
          aiMessage.content = currentStreamContent.value;
          // 移除自动滚动，让用户可以自由查看生成内容
        }
      } catch (error) {
        console.error('解析SSE消息失败:', error);
      }
    };
    
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error);
      eventSource.close();
      isGenerating.value = false;
      // 清空输入框，准备下一次对话
      userInput.value = '';
    };
    
    eventSource.addEventListener('done', () => {
      eventSource.close();
      isGenerating.value = false;
      // 清空输入框，准备下一次对话
      userInput.value = '';
      // 生成完成时不自动滚动，让用户自由查看
    });
    
  } catch (error) {
    console.error('发送消息失败:', error)
    isGenerating.value = false
    // 清空输入框，准备下一次对话
    userInput.value = '';
    alert('发送消息失败，请重试')
  }
}

// 部署应用
const handleDeploy = async () => {
  if (!appInfo.value?.id) return
  
  isDeploying.value = true
  try {
    const response = await deployApp({ appId: appInfo.value.id })
    
    if (response.data.code === 0) {
      deployedUrl.value = response.data.data
      showDeploySuccessModal.value = true
    } else {
      throw new Error(response.data.message || '部署失败')
    }
  } catch (error) {
    console.error('部署失败:', error)
    alert('部署失败，请重试')
  } finally {
    isDeploying.value = false
  }
}

// 直接访问部署的应用
const handleDirectAccess = () => {
  if (deployedUrl.value) {
    window.open(deployedUrl.value, '_blank')
  }
}

// 复制访问链接
const handleCopyLink = async () => {
  if (deployedUrl.value) {
    try {
      await navigator.clipboard.writeText(deployedUrl.value)
      alert('链接已复制到剪贴板')
    } catch (error) {
      console.error('复制失败:', error)
      alert('复制失败，请手动复制')
    }
  }
}

// 重置生成状态
const resetGeneratingState = () => {
  isGenerating.value = false
  currentStreamContent.value = ''
  console.log('生成状态已重置')
}

// 处理发送按钮点击
const handleSendClick = () => {
  console.log('发送按钮被点击')
  console.log('用户输入内容:', userInput.value)
  console.log('输入内容是否为空:', !userInput.value?.trim())
  
  if (!userInput.value?.trim()) {
    console.log('输入内容为空，不发送')
    return
  }
  
  console.log('开始发送消息')
  sendMessage(userInput.value)
}

// 滚动到底部
const scrollToBottom = () => {
  const messagesContainer = document.querySelector('.messages-container')
  if (messagesContainer) {
    messagesContainer.scrollTop = messagesContainer.scrollHeight
  }
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 格式化日期
const formatDate = (dateString: string | undefined) => {
  if (!dateString) return '未知'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return '未知'
  }
}

// 解析markdown内容
const parseMarkdown = (content: string) => {
  try {
    return marked(content)
  } catch (error) {
    console.error('Markdown解析失败:', error)
    return content
  }
}
</script>

<template>
  <div class="app-chat-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="app-info">
        <button @click="router.push('/')" class="back-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12,19 5,12 12,5"></polyline>
          </svg>
          返回
        </button>
        <div class="app-name-container">
          <span class="app-name">{{ appInfo?.appName || '加载中...' }}</span>
          <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6,9 12,15 18,9"></polyline>
          </svg>
        </div>
        <span class="app-type">应用名称</span>
      </div>
      
      <div class="deploy-section">
        <button @click="router.push(`/app/edit/${appId}`)" class="edit-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
          </svg>
          编辑
        </button>
        <span class="deploy-label">部署按钮</span>
        <button 
          @click="handleDeploy"
          :disabled="isDeploying || !appInfo"
          class="deploy-btn"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z"></path>
          </svg>
          部署
        </button>
      </div>
    </div>

    <!-- 核心内容区域 -->
    <div class="main-content">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 标签页 -->
        <div class="tabs">
          <button class="tab-btn active">用户消息</button>
        </div>
        
        <!-- 消息区域 -->
        <div class="messages-container">
          <div 
            v-for="message in messages" 
            :key="message.id"
            :class="['message', message.type]"
          >
            <div class="message-avatar">
              <svg v-if="message.type === 'ai'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="parseMarkdown(message.content)"></div>
              <div class="message-time">
                <span v-if="message.type === 'ai' && isGenerating && message.id === messages[messages.length - 1]?.id">
                  正在生成... ({{ currentStreamContent.length }}字符)
                </span>
                <span v-else>
                  {{ formatTime(message.timestamp) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
                <!-- 用户输入框 -->
        <div class="input-section">
          <!-- 移除权限提示 -->
          <!-- <div v-if="!canEdit && isLoggedIn" class="permission-notice">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <span>您正在查看其他用户的作品，无法进行编辑。只有作品所有者才能修改此作品。</span>
          </div> -->
          <textarea
            v-model="userInput"
            placeholder="描述越详细,页面越具体,可以一步一步完善生成效果"
            class="user-input"
            rows="3"
            @keydown.ctrl.enter="sendMessage(userInput.value)"
          ></textarea>
          <div class="input-actions">
                                      <button 
              class="send-btn"
              @click="handleSendClick"
              :disabled="false"
              :title="!userInput.value?.trim() ? '请输入内容' : '发送消息'"
              style="pointer-events: auto !important; cursor: pointer !important; background: #1890ff !important;"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22,2 15,22 11,13 2,9"></polygon>
              </svg>
            </button>

            <button 
              v-if="isGenerating"
              @click="resetGeneratingState"
              class="reset-btn"
              title="重置生成状态"
            >
              重置
            </button>
          </div>
        </div>
      </div>
      
      <!-- 右侧网页展示区域 -->
      <div class="preview-section">
        <div class="preview-header">
          <h3>网页预览</h3>
        </div>
        <div class="preview-content">
          <div v-if="appInfo?.codeGenType && appInfo?.id" class="web-preview">
            <iframe 
              :src="`http://localhost:8123/api/static/${appInfo.codeGenType}_${appInfo.id}/`"
              frameborder="0"
              class="preview-iframe"
            ></iframe>
          </div>
          <div v-else class="preview-placeholder">
            <div class="placeholder-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </div>
            <p>等待生成完成...</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 部署成功弹窗 -->
    <div v-if="showDeploySuccessModal" class="modal-overlay" @click="showDeploySuccessModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>部署成功！</h3>
          <button class="modal-close" @click="showDeploySuccessModal = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="success-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22,4 12,14.01 9,11.01"></polyline>
            </svg>
          </div>
          <p class="success-message">应用部署成功！</p>
          <div class="url-section">
            <label>访问地址：</label>
            <div class="url-display">
              <span class="url-text">{{ deployedUrl }}</span>
              <button class="copy-btn" @click="handleCopyLink" title="复制链接">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showDeploySuccessModal = false">
            关闭
          </button>
          <button class="btn-primary" @click="handleDirectAccess">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
              <polyline points="15,3 21,3 21,9"></polyline>
              <line x1="10" y1="14" x2="21" y2="3"></line>
            </svg>
            直接访问
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
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

.app-name-container {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.dropdown-arrow {
  width: 16px;
  height: 16px;
  color: #666;
  transition: transform 0.2s;
}

.app-name-container:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.app-type {
  font-size: 14px;
  color: #666;
}

.deploy-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.edit-btn {
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

.edit-btn:hover {
  background: #f5f5f5;
  border-color: #52c41a;
  color: #52c41a;
}

.edit-btn svg {
  width: 16px;
  height: 16px;
}

.deploy-label {
  font-size: 14px;
  color: #666;
}

.deploy-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.deploy-btn:hover:not(:disabled) {
  background: #40a9ff;
  transform: translateY(-1px);
}

.deploy-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.deploy-btn svg {
  width: 16px;
  height: 16px;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-right: 1px solid #e8e8e8;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
}

.tab-btn {
  padding: 12px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-btn.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.tab-btn:hover {
  color: #1890ff;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #1890ff;
  color: white;
}

.message-avatar svg {
  width: 20px;
  height: 20px;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f5f5;
  color: #333;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .message-text {
  background: #1890ff;
  color: white;
}

/* Markdown样式 */
.message-text :deep(pre) {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-text :deep(code) {
  background: #f6f8fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
  border-radius: 0;
}

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin: 16px 0 8px 0;
  font-weight: 600;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text :deep(li) {
  margin: 4px 0;
}

.message-text :deep(blockquote) {
  border-left: 4px solid #dfe2e5;
  padding-left: 16px;
  margin: 8px 0;
  color: #6a737d;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.message.user .message-time {
  text-align: right;
}

.input-section {
  padding: 20px;
  border-top: 1px solid #e8e8e8;
}

.user-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  outline: none;
  transition: border-color 0.2s;
}

.user-input:focus {
  border-color: #1890ff;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 12px;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f5f5f5;
  border-color: #1890ff;
  color: #1890ff;
}

.action-btn svg {
  width: 14px;
  height: 14px;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1890ff;
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  position: relative;
  z-index: 1000;
  pointer-events: auto !important;
}

.send-btn:hover:not(:disabled) {
  background: #40a9ff;
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #d9d9d9;
  pointer-events: none;
}

.send-btn svg {
  width: 16px;
  height: 16px;
}

/* 确保发送按钮可以点击 */
.send-btn:not(:disabled) {
  pointer-events: auto !important;
  cursor: pointer !important;
  z-index: 1001 !important;
}

.reset-btn {
  padding: 6px 12px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-left: 8px;
  transition: all 0.2s;
}

.reset-btn:hover {
  background: #ff7875;
}

/* 权限提示样式 */
.permission-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 6px;
  margin-bottom: 12px;
  color: #d46b08;
  font-size: 14px;
}

.permission-notice svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.user-input.disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}



.preview-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.preview-content {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}

.web-preview {
  width: 100%;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.placeholder-icon {
  width: 60px;
  height: 60px;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.placeholder-icon svg {
  width: 30px;
  height: 30px;
  color: #ccc;
}

.preview-placeholder p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .chat-section {
    height: 60%;
  }
  
  .preview-section {
    height: 40%;
  }
  
  .message-content {
    max-width: 85%;
  }
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #666;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.modal-body {
  padding: 24px;
  text-align: center;
}

.success-icon {
  width: 60px;
  height: 60px;
  background: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.success-icon svg {
  width: 30px;
  height: 30px;
  color: white;
}

.success-message {
  font-size: 16px;
  color: #333;
  margin: 0 0 20px 0;
  font-weight: 500;
}

.url-section {
  text-align: left;
  margin-top: 20px;
}

.url-section label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.url-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.url-text {
  flex: 1;
  font-size: 13px;
  color: #333;
  word-break: break-all;
}

.copy-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #666;
  transition: all 0.2s;
  flex-shrink: 0;
}

.copy-btn:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.copy-btn svg {
  width: 16px;
  height: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.btn-secondary {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: #f5f5f5;
  border-color: #1890ff;
  color: #1890ff;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #40a9ff;
  transform: translateY(-1px);
}

.btn-primary svg {
  width: 16px;
  height: 16px;
}
</style>
