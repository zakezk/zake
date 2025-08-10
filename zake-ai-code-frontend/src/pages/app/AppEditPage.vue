<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById, updateMyApp, updateApp } from '@/api/appController'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO | null>(null)
const loading = ref(true)

// 编辑表单
const form = ref({
  id: '',
  appName: '',
})

// 提交状态
const submitting = ref(false)

// 计算属性
const appId = computed(() => String(route.params.appId))
const isLoggedIn = computed(() => !!loginUserStore.loginUser.userAccount)
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
const isOwner = computed(() => {
  if (!appInfo.value || !isLoggedIn.value) return false
  return appInfo.value.userId === loginUserStore.loginUser.id
})

// 初始化
onMounted(async () => {
  if (!isLoggedIn.value) {
    alert('请先登录')
    router.push('/user/login')
    return
  }

  await loadAppInfo()
})

// 加载应用信息
const loadAppInfo = async () => {
  try {
    loading.value = true
    const response = await getAppVoById({ id: appId.value })

    if (response.data.code === 0 && response.data.data) {
      appInfo.value = response.data.data

      // 检查权限
      if (!isAdmin.value && !isOwner.value) {
        alert('您没有权限编辑此应用')
        router.push('/')
        return
      }

      // 填充表单
      form.value = {
        id: appInfo.value?.id || '',
        appName: appInfo.value?.appName || '',
      }
    } else {
      throw new Error(response.data.message || '加载应用信息失败')
    }
  } catch (error) {
    console.error('加载应用信息失败:', error)
    alert('加载应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 保存修改
const handleSave = async () => {
  // 检查权限
  if (!isAdmin.value && !isOwner.value) {
    alert('您没有权限修改此应用')
    return
  }

  if (!form.value.appName.trim()) {
    alert('请输入应用名称')
    return
  }

  submitting.value = true
  try {
    // 根据用户角色调用不同的API
    const updateFunction = isAdmin.value ? updateApp : updateMyApp
    const response = await updateFunction(form.value)

    if (response.data.code === 0) {
      alert('保存成功')
      router.push(`/app/chat/${appId.value}`)
    } else {
      throw new Error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.push(`/app/chat/${appId.value}`)
}

// 预览应用
const previewApp = () => {
  router.push(`/app/chat/${appId.value}`)
}
</script>

<template>
  <div class="app-edit-page">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h1>编辑应用</h1>
          <p>修改应用的基本信息</p>
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

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="appInfo" class="edit-form">
      <!-- 应用预览 -->
      <div class="app-preview">
        <div class="preview-header">
          <h3>应用预览</h3>
        </div>
        <div class="preview-content">
          <div class="app-card">
            <img
              v-if="appInfo.cover"
              :src="appInfo.cover"
              :alt="appInfo.appName"
              class="app-cover"
            />
            <div class="app-info">
              <h4 class="app-name">{{ appInfo.appName }}</h4>
              <p class="app-creator">创建者: {{ appInfo.user?.userName || '未知' }}</p>
              <p class="app-time">
                创建时间: {{ new Date(appInfo.createTime || '').toLocaleString('zh-CN') }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑表单 -->
      <div class="form-section">
        <div class="form-header">
          <h3>基本信息</h3>
        </div>
        <div class="form-content">
          <div class="form-item">
            <label>应用名称 *</label>
            <input
              v-model="form.appName"
              placeholder="请输入应用名称"
              class="form-input"
              maxlength="50"
              :disabled="!isAdmin && !isOwner"
            />
            <div class="form-tip">
              {{
                isAdmin || isOwner
                  ? '应用名称不能超过50个字符'
                  : '只有管理员和所有者可以修改应用名称'
              }}
            </div>
          </div>

          <div class="form-item">
            <label>作者名字</label>
            <input :value="appInfo.user?.userName || '未知'" class="form-input" disabled />
            <div class="form-tip">作者名字不可修改</div>
          </div>

          <div class="form-item">
            <label>创建时间</label>
            <input
              :value="new Date(appInfo.createTime || '').toLocaleString('zh-CN')"
              class="form-input"
              disabled
            />
            <div class="form-tip">创建时间不可修改</div>
          </div>

          <div class="form-item">
            <label>代码类型</label>
            <input :value="appInfo.codeGenType || '-'" class="form-input" disabled />
            <div class="form-tip">代码类型不可修改</div>
          </div>

          <div class="form-item">
            <label>初始提示词</label>
            <textarea
              :value="appInfo.initPrompt || ''"
              class="form-textarea"
              disabled
              rows="4"
            ></textarea>
            <div class="form-tip">初始提示词不可修改</div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button @click="handleCancel" class="cancel-btn">取消</button>
        <button @click="previewApp" class="preview-btn">预览应用</button>
        <button @click="router.push(`/app/chat/${appId}`)" class="chat-btn">返回对话</button>
        <button
          v-if="isAdmin || isOwner"
          @click="handleSave"
          :disabled="submitting || !form.appName.trim()"
          class="save-btn"
        >
          {{ submitting ? '保存中...' : '保存修改' }}
        </button>
      </div>
    </div>

    <div v-else class="error-state">
      <div class="error-icon">⚠️</div>
      <h3>应用不存在</h3>
      <p>您要编辑的应用不存在或已被删除</p>
      <button @click="router.push('/')" class="back-btn">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.app-edit-page {
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

.loading-state,
.error-state {
  text-align: center;
  padding: 60px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-state h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.error-state p {
  margin: 0 0 24px 0;
  color: #666;
}

.back-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #40a9ff;
}

.edit-form {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.app-preview {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.preview-content {
  padding: 20px;
}

.app-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.app-cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
  background: #f8f9fa;
}

.app-info {
  padding: 16px;
}

.app-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.app-creator,
.app-time {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #666;
}

.form-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.form-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.form-content {
  padding: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  border-color: #1890ff;
}

.form-input:disabled,
.form-textarea:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.cancel-btn,
.preview-btn,
.save-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e8e8e8;
}

.preview-btn {
  background: #52c41a;
  color: white;
}

.preview-btn:hover {
  background: #73d13d;
}

.chat-btn {
  background: #1890ff;
  color: white;
}

.chat-btn:hover {
  background: #40a9ff;
}

.save-btn {
  background: #1890ff;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .edit-form {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-btn,
  .preview-btn,
  .save-btn {
    width: 100%;
  }
}
</style>
