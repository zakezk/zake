<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { listAppByPage, deleteApp, updateApp } from '@/api/appController'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用列表
const apps = ref<API.AppVO[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索条件
const searchForm = ref({
  appName: '',
  codeGenType: '',
  userId: '',
  priority: '',
})

// 编辑相关
const editDialogVisible = ref(false)
const editForm = ref({
  id: '',
  appName: '',
  cover: '',
  priority: 0,
})

// 删除相关
const deleteDialogVisible = ref(false)
const deletingApp = ref<API.AppVO | null>(null)

// 计算属性
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 初始化
onMounted(() => {
  if (!isAdmin.value) {
    alert('您没有权限访问此页面')
    router.push('/')
    return
  }
  loadApps()
})

// 加载应用列表
const loadApps = async () => {
  loading.value = true
  try {
    const response = await listAppByPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appName: searchForm.value.appName || undefined,
      codeGenType: searchForm.value.codeGenType || undefined,
      userId: searchForm.value.userId || undefined,
      priority: searchForm.value.priority ? Number(searchForm.value.priority) : undefined,
    })

    if (response.data.code === 0 && response.data.data) {
      apps.value = response.data.data.records || []
      total.value = response.data.data.totalRow || 0
    } else {
      throw new Error(response.data.message || '加载应用列表失败')
    }
  } catch (error) {
    console.error('加载应用列表失败:', error)
    alert('加载应用列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadApps()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    appName: '',
    codeGenType: '',
    userId: '',
    priority: '',
  }
  currentPage.value = 1
  loadApps()
}

// 编辑应用
const handleEdit = (app: API.AppVO) => {
  editForm.value = {
    id: app.id || '',
    appName: app.appName || '',
    cover: app.cover || '',
    priority: app.priority || 0,
  }
  editDialogVisible.value = true
}

// 保存编辑
const saveEdit = async () => {
  try {
    const response = await updateApp({
      ...editForm.value,
      id: editForm.value.id,
    })

    if (response.data.code === 0) {
      alert('更新成功')
      editDialogVisible.value = false
      loadApps()
    } else {
      throw new Error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新应用失败:', error)
    alert('更新失败，请重试')
  }
}

// 删除应用
const handleDelete = (app: API.AppVO) => {
  deletingApp.value = app
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!deletingApp.value?.id) return

  try {
    const response = await deleteApp({ id: deletingApp.value.id })

    if (response.data.code === 0) {
      alert('删除成功')
      deleteDialogVisible.value = false
      loadApps()
    } else {
      throw new Error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    alert('删除失败，请重试')
  }
}

// 设置精选
const setFeatured = async (app: API.AppVO) => {
  try {
    const response = await updateApp({
      id: app.id,
      appName: app.appName,
      priority: 99,
    })

    if (response.data.code === 0) {
      alert('设置精选成功')
      loadApps()
    } else {
      throw new Error(response.data.message || '设置精选失败')
    }
  } catch (error) {
    console.error('设置精选失败:', error)
    alert('设置精选失败，请重试')
  }
}

// 查看应用详情
const viewApp = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}`)
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

// 处理上一页
const handlePrevPage = () => {
  currentPage.value--
  loadApps()
}

// 处理下一页
const handleNextPage = () => {
  currentPage.value++
  loadApps()
}
</script>

<template>
  <div class="app-manage-page">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h1>应用管理</h1>
          <p>管理所有用户创建的应用</p>
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
          <label>应用名称:</label>
          <input v-model="searchForm.appName" placeholder="搜索应用名称" class="search-input" />
        </div>
        <div class="form-item">
          <label>代码类型:</label>
          <input v-model="searchForm.codeGenType" placeholder="搜索代码类型" class="search-input" />
        </div>
        <div class="form-item">
          <label>用户ID:</label>
          <input v-model="searchForm.userId" placeholder="搜索用户ID" class="search-input" />
        </div>
        <div class="form-item">
          <label>优先级:</label>
          <input v-model="searchForm.priority" placeholder="搜索优先级" class="search-input" />
        </div>
        <div class="form-actions">
          <button @click="handleSearch" class="search-btn">搜索</button>
          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>
    </div>

    <!-- 应用列表 -->
    <div class="apps-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>应用名称</th>
            <th>创建者</th>
            <th>代码类型</th>
            <th>优先级</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="app in apps" :key="app.id">
            <td>{{ app.id }}</td>
            <td>
              <div class="app-info">
                <img
                  :src="app.cover || '/default-app-cover.png'"
                  :alt="app.appName"
                  class="app-cover"
                />
                <span class="app-name">{{ app.appName }}</span>
              </div>
            </td>
            <td>{{ app.user?.userName || '未知用户' }}</td>
            <td>{{ app.codeGenType || '-' }}</td>
            <td>
              <span :class="['priority', app.priority === 99 ? 'featured' : '']">
                {{ app.priority || 0 }}
              </span>
            </td>
            <td>{{ formatTime(app.createTime || '') }}</td>
            <td>
              <div class="action-buttons">
                <button @click="viewApp(app)" class="action-btn view-btn">查看</button>
                <button @click="handleEdit(app)" class="action-btn edit-btn">编辑</button>
                <button @click="setFeatured(app)" class="action-btn feature-btn">精选</button>
                <button @click="handleDelete(app)" class="action-btn delete-btn">删除</button>
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
      <div v-if="!loading && apps.length === 0" class="empty-state">
        <p>暂无应用数据</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <button @click="handlePrevPage" :disabled="currentPage <= 1" class="page-btn">上一页</button>
      <span class="page-info"> {{ currentPage }} / {{ Math.ceil(total / pageSize) }} </span>
      <button
        @click="handleNextPage"
        :disabled="currentPage >= Math.ceil(total / pageSize)"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="editDialogVisible" class="modal-overlay" @click="editDialogVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑应用</h3>
          <button @click="editDialogVisible = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>应用名称:</label>
            <input v-model="editForm.appName" class="form-input" />
          </div>
          <div class="form-item">
            <label>应用封面:</label>
            <input v-model="editForm.cover" class="form-input" />
          </div>
          <div class="form-item">
            <label>优先级:</label>
            <input v-model="editForm.priority" type="number" class="form-input" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="editDialogVisible = false" class="cancel-btn">取消</button>
          <button @click="saveEdit" class="save-btn">保存</button>
        </div>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <div v-if="deleteDialogVisible" class="modal-overlay" @click="deleteDialogVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>确认删除</h3>
          <button @click="deleteDialogVisible = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <p>确定要删除应用 "{{ deletingApp?.appName }}" 吗？此操作不可恢复。</p>
        </div>
        <div class="modal-footer">
          <button @click="deleteDialogVisible = false" class="cancel-btn">取消</button>
          <button @click="confirmDelete" class="delete-btn">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-manage-page {
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

.search-btn,
.reset-btn {
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

.apps-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-cover {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}

.app-name {
  font-weight: 500;
  color: #333;
}

.priority {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.priority.featured {
  background: #fff7e6;
  color: #fa8c16;
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

.edit-btn {
  background: #52c41a;
  color: white;
}

.edit-btn:hover {
  background: #73d13d;
}

.feature-btn {
  background: #fa8c16;
  color: white;
}

.feature-btn:hover {
  background: #ffa940;
}

.delete-btn {
  background: #ff4d4f;
  color: white;
}

.delete-btn:hover {
  background: #ff7875;
}

.loading-state,
.empty-state {
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  min-width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 24px;
}

.modal-body .form-item {
  margin-bottom: 16px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: #1890ff;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn,
.save-btn,
.delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e8e8e8;
}

.save-btn {
  background: #1890ff;
  color: white;
}

.save-btn:hover {
  background: #40a9ff;
}

.modal-footer .delete-btn {
  background: #ff4d4f;
  color: white;
}

.modal-footer .delete-btn:hover {
  background: #ff7875;
}

@media (max-width: 768px) {
  .search-form {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .modal-content {
    min-width: 90%;
  }
}
</style>
