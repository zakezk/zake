<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { createApp, listMyAppByPage, listFeaturedAppByPage, deleteMyApp } from '@/api/appController'

const loginUserStore = useLoginUserStore()
const route = useRoute()
const router = useRouter()
const showLoginSuccess = ref(false)

// 用户提示词输入
const userPrompt = ref('')
const isCreating = ref(false)

// 我的应用列表
const myApps = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const myAppsTotal = ref(0)
const myAppsCurrentPage = ref(1)
const myAppsPageSize = ref(20)
const myAppsSearchName = ref('')

// 精选应用列表
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsTotal = ref(0)
const featuredAppsCurrentPage = ref(1)
const featuredAppsPageSize = ref(20)
const featuredAppsSearchName = ref('')
const hoveredAppId = ref<string | null>(null)

const isLoggedIn = computed(() => {
  return !!loginUserStore.loginUser.userAccount
})

const userName = computed(() => {
  return loginUserStore.loginUser.userAccount || loginUserStore.loginUser.userName || '用户'
})

// 设置用户提示词
const setUserPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 处理我的应用上一页
const handleMyAppsPrevPage = () => {
  myAppsCurrentPage.value--
  loadMyApps()
}

// 处理我的应用下一页
const handleMyAppsNextPage = () => {
  myAppsCurrentPage.value++
  loadMyApps()
}

// 处理精选应用上一页
const handleFeaturedAppsPrevPage = () => {
  featuredAppsCurrentPage.value--
  loadFeaturedApps()
}

// 处理精选应用下一页
const handleFeaturedAppsNextPage = () => {
  featuredAppsCurrentPage.value++
  loadFeaturedApps()
}

// 检查是否从登录页面跳转过来
onMounted(() => {
  const fromLogin = route.query.from === 'login'
  if (fromLogin && isLoggedIn.value) {
    showLoginSuccess.value = true
    // 0.5秒后自动隐藏
    setTimeout(() => {
      showLoginSuccess.value = false
    }, 500)
  }

  // 加载应用列表
  loadMyApps()
  loadFeaturedApps()
})

// 创建应用
const handleCreateApp = async () => {
  if (!userPrompt.value.trim()) {
    alert('请输入应用描述')
    return
  }

  if (!isLoggedIn.value) {
    alert('请先登录')
    return
  }

  isCreating.value = true
  try {
    const response = await createApp({
      initPrompt: userPrompt.value,
    })

    if (response.data.code === 0) {
      const appId = response.data.data
      // 跳转到对话页面，并传递prompt
      router.push({ path: `/app/chat/${appId}`, query: { prompt: userPrompt.value } })
    } else {
      alert(response.data.message || '创建应用失败')
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    alert('创建应用失败，请重试')
  } finally {
    isCreating.value = false
  }
}

// 加载我的应用列表
const loadMyApps = async () => {
  if (!isLoggedIn.value) return

  myAppsLoading.value = true
  try {
    const response = await listMyAppByPage({
      pageNum: myAppsCurrentPage.value,
      pageSize: myAppsPageSize.value,
      appName: myAppsSearchName.value || undefined,
    })

    if (response.data.code === 0) {
      myApps.value = response.data.data?.records || []
      myAppsTotal.value = response.data.data?.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败:', error)
  } finally {
    myAppsLoading.value = false
  }
}

// 加载精选应用列表
const loadFeaturedApps = async () => {
  featuredAppsLoading.value = true
  try {
    const response = await listFeaturedAppByPage({
      pageNum: featuredAppsCurrentPage.value,
      pageSize: featuredAppsPageSize.value,
      appName: featuredAppsSearchName.value || undefined,
    })

    if (response.data.code === 0) {
      featuredApps.value = response.data.data?.records || []
      featuredAppsTotal.value = response.data.data?.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败:', error)
  } finally {
    featuredAppsLoading.value = false
  }
}

// 搜索我的应用
const searchMyApps = () => {
  myAppsCurrentPage.value = 1
  loadMyApps()
}

// 搜索精选应用
const searchFeaturedApps = () => {
  featuredAppsCurrentPage.value = 1
  loadFeaturedApps()
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return `${days}天前`
  }
}

// 删除我的应用
const handleDeleteMyApp = async (app: API.AppVO) => {
  if (!confirm(`确定要删除应用 "${app.appName}" 吗？`)) {
    return
  }

  try {
    const response = await deleteMyApp({ id: app.id })

    if (response.data.code === 0) {
      alert('删除成功')
      loadMyApps()
    } else {
      throw new Error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    alert('删除失败，请重试')
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 显示作品悬浮提示
const showAppTooltip = (appId: string | undefined) => {
  hoveredAppId.value = appId || null
}

// 隐藏作品悬浮提示
const hideAppTooltip = () => {
  hoveredAppId.value = null
}

// 预览作品
const previewApp = (app: API.AppVO) => {
  if (app.codeGenType && app.id) {
    // 如果是 Vue 项目，浏览地址需要添加 dist/index.html 后缀
    const suffix = app.codeGenType === 'vue_project' ? '/dist/index.html' : ''
    const previewUrl = `http://localhost:8123/api/static/${app.codeGenType}_${app.id}${suffix}`
    window.open(previewUrl, '_blank')
  } else {
    alert('该作品暂无可预览的内容')
  }
}
</script>

<template>
  <div class="home">
    <!-- 网站标题区域 -->
    <div class="header-section">
      <div class="title-container">
        <h1 class="main-title">
          一句话 呈所想
          <span class="cat-icon">🐱</span>
        </h1>
        <p class="subtitle">与 AI 对话轻松创建应用和网站</p>
      </div>
    </div>

    <!-- 用户提示词输入框 -->
    <div class="prompt-section">
      <div class="prompt-container">
        <textarea
          v-model="userPrompt"
          placeholder="使用 NoCode 创建一个高效的小工具,帮我计算......"
          class="prompt-input"
          rows="3"
        ></textarea>
        <div class="prompt-actions">
          <div class="left-actions">
            <button @click="userPrompt = '请根据我上传的文件内容来生成应用'" class="action-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path
                  d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"
                ></path>
              </svg>
              上传
            </button>
            <button
              @click="userPrompt = '请优化我的应用需求描述，使其更加详细和具体'"
              class="action-btn"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="5"></circle>
                <line x1="12" y1="1" x2="12" y2="3"></line>
                <line x1="12" y1="21" x2="12" y2="23"></line>
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
                <line x1="1" y1="12" x2="3" y2="12"></line>
                <line x1="21" y1="12" x2="23" y2="12"></line>
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
              </svg>
              优化
            </button>
          </div>
          <button
            @click="handleCreateApp"
            :disabled="isCreating || !userPrompt.trim()"
            class="create-btn"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 建议分类 -->
    <div class="categories-section">
      <div class="category-buttons">
        <button
          @click="setUserPrompt('创建一个波普风格的电商页面，包含商品展示、购物车、用户登录等功能')"
          class="category-btn"
        >
          波普风电商页面
        </button>
        <button
          @click="
            setUserPrompt('创建一个企业官网，包含公司介绍、产品服务、团队介绍、联系方式等页面')
          "
          class="category-btn"
        >
          企业网站
        </button>
        <button
          @click="
            setUserPrompt(
              '创建一个电商运营后台管理系统，包含订单管理、商品管理、用户管理、数据统计等功能',
            )
          "
          class="category-btn"
        >
          电商运营后台
        </button>
        <button
          @click="
            setUserPrompt('创建一个暗黑风格的话题社区，用户可以发布帖子、评论、点赞，支持话题分类')
          "
          class="category-btn"
        >
          暗黑话题社区
        </button>
      </div>
    </div>

    <!-- 我的应用列表 -->
    <div class="apps-section" v-if="isLoggedIn">
      <div class="section-header">
        <h2>我的作品</h2>
        <div class="search-box">
          <input
            v-model="myAppsSearchName"
            placeholder="搜索应用名称"
            @keyup.enter="searchMyApps"
            class="search-input"
          />
          <button @click="searchMyApps" class="search-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
          </button>
        </div>
      </div>

      <div class="apps-grid" v-if="!myAppsLoading">
        <div
          v-for="app in myApps"
          :key="app.id"
          class="app-card"
          @mouseenter="showAppTooltip(app.id)"
          @mouseleave="hideAppTooltip()"
        >
          <div class="app-preview" @click="router.push(`/app/chat/${app.id || ''}`)">
            <img
              :src="app.cover || '/default-app-cover.png'"
              :alt="app.appName || ''"
              class="app-cover"
            />
          </div>
          <div class="app-info">
            <h3 class="app-name" @click="router.push(`/app/chat/${app.id || ''}`)">
              {{ app.appName || '' }}
            </h3>
            <p class="app-time">创建于{{ formatTime(app.createTime || '') }}</p>
            <div class="app-actions">
              <button @click="router.push(`/app/edit/${app.id || ''}`)" class="action-btn edit-btn">
                编辑
              </button>
              <button @click="handleDeleteMyApp(app)" class="action-btn delete-btn">删除</button>
            </div>
          </div>

          <!-- 悬浮提示 -->
          <div v-if="hoveredAppId === app.id" class="app-tooltip">
            <div class="tooltip-content">
              <h4>作品详情</h4>
              <p><strong>作品名称：</strong>{{ app.appName || '' }}</p>
              <p><strong>创建时间：</strong>{{ formatDate(app.createTime || '') }}</p>
              <p><strong>作品类型：</strong>{{ app.codeGenType || '未知' }}</p>
              <p><strong>状态：</strong>{{ app.deployKey ? '已部署' : '未部署' }}</p>
              <div class="tooltip-actions">
                <button
                  @click="router.push(`/app/chat/${app.id || ''}`)"
                  class="tooltip-btn primary"
                >
                  查看对话
                </button>
                <button @click="previewApp(app)" class="tooltip-btn secondary">预览作品</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-state" v-if="myAppsLoading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div class="empty-state" v-if="!myAppsLoading && myApps.length === 0">
        <p>暂无应用，开始创建您的第一个应用吧！</p>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="myAppsTotal > 0">
        <button @click="handleMyAppsPrevPage" :disabled="myAppsCurrentPage <= 1" class="page-btn">
          上一页
        </button>
        <span class="page-info"
          >{{ myAppsCurrentPage }} / {{ Math.ceil(myAppsTotal / myAppsPageSize) }}</span
        >
        <button
          @click="handleMyAppsNextPage"
          :disabled="myAppsCurrentPage >= Math.ceil(myAppsTotal / myAppsPageSize)"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 精选应用列表 -->
    <div class="apps-section">
      <div class="section-header">
        <h2>精选案例</h2>
        <div class="search-box">
          <input
            v-model="featuredAppsSearchName"
            placeholder="搜索应用名称"
            @keyup.enter="searchFeaturedApps"
            class="search-input"
          />
          <button @click="searchFeaturedApps" class="search-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
          </button>
        </div>
      </div>

      <div class="apps-grid" v-if="!featuredAppsLoading">
        <div
          v-for="app in featuredApps"
          :key="app.id"
          class="app-card"
          @click="router.push(`/app/chat/${app.id}`)"
        >
          <div class="app-preview">
            <img
              :src="app.cover || '/default-app-cover.png'"
              :alt="app.appName"
              class="app-cover"
            />
          </div>
          <div class="app-info">
            <h3 class="app-name">{{ app.appName }}</h3>
            <p class="app-creator">作者：{{ app.user?.userName || '未知用户' }}</p>
          </div>
        </div>
      </div>

      <div class="loading-state" v-if="featuredAppsLoading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div class="empty-state" v-if="!featuredAppsLoading && featuredApps.length === 0">
        <p>暂无精选应用</p>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="featuredAppsTotal > 0">
        <button
          @click="handleFeaturedAppsPrevPage"
          :disabled="featuredAppsCurrentPage <= 1"
          class="page-btn"
        >
          上一页
        </button>
        <span class="page-info"
          >{{ featuredAppsCurrentPage }} /
          {{ Math.ceil(featuredAppsTotal / featuredAppsPageSize) }}</span
        >
        <button
          @click="handleFeaturedAppsNextPage"
          :disabled="featuredAppsCurrentPage >= Math.ceil(featuredAppsTotal / featuredAppsPageSize)"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 登录成功提示弹窗 -->
    <div v-if="showLoginSuccess" class="login-success-modal">
      <div class="modal-content">
        <div class="success-icon">✓</div>
        <h3>登录成功！</h3>
        <p>欢迎回来，{{ userName }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

/* 头部区域 */
.header-section {
  text-align: center;
  margin-bottom: 40px;
  padding-top: 40px;
}

.title-container {
  max-width: 600px;
  margin: 0 auto;
}

.main-title {
  font-size: 3rem;
  font-weight: bold;
  color: white;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.cat-icon {
  font-size: 2.5rem;
}

.subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

/* 提示词输入区域 */
.prompt-section {
  max-width: 800px;
  margin: 0 auto 40px;
}

.prompt-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
}

.prompt-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 16px;
  line-height: 1.5;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.prompt-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.left-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.action-btn:hover {
  background: #f5f5f5;
  border-color: #d0d0d0;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

.create-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.create-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.create-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.create-btn svg {
  width: 20px;
  height: 20px;
}

/* 分类按钮 */
.categories-section {
  max-width: 800px;
  margin: 0 auto 40px;
}

.category-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  justify-content: center;
}

.category-btn {
  padding: 12px 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  backdrop-filter: blur(10px);
}

.category-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-1px);
}

/* 应用列表区域 */
.apps-section {
  max-width: 1200px;
  margin: 0 auto 60px;
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.section-header h2 {
  font-size: 1.8rem;
  color: #333;
  margin: 0;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
  min-width: 200px;
}

.search-input:focus {
  border-color: #667eea;
}

.search-btn {
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.search-btn:hover {
  background: #f5f5f5;
}

.search-btn svg {
  width: 16px;
  height: 16px;
}

/* 应用网格 */
.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.app-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.app-preview {
  height: 200px;
  overflow: hidden;
  background: #f8f9fa;
}

.app-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-info {
  padding: 20px;
}

.app-name {
  font-size: 1.1rem;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.app-time,
.app-creator {
  font-size: 0.9rem;
  color: #666;
  margin: 0;
}

.app-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.edit-btn {
  background: #52c41a;
  color: white;
}

.edit-btn:hover {
  background: #73d13d;
}

.delete-btn {
  background: #ff4d4f;
  color: white;
}

.delete-btn:hover {
  background: #ff7875;
}

/* 加载和空状态 */
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
  border-top: 3px solid #667eea;
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

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
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

/* 登录成功弹窗 */
.login-success-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease-out;
  max-width: 300px;
  width: 90%;
}

.success-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 30px;
  color: white;
  font-weight: bold;
}

.modal-content h3 {
  color: #333;
  margin-bottom: 10px;
  font-size: 18px;
}

.modal-content p {
  color: #666;
  font-size: 14px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 作品悬浮提示样式 */
.app-card {
  position: relative;
}

.app-tooltip {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  background: transparent;
  color: #333;
  border-radius: 8px;
  padding: 16px;
  min-width: 280px;
  animation: tooltipFadeIn 0.3s ease-out;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.tooltip-content h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #000;
  text-align: center;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.8);
}

.tooltip-content p {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.4;
  color: #000;
  text-shadow: 0 0 2px rgba(255, 255, 255, 0.8);
}

.tooltip-content strong {
  color: #1890ff;
  text-shadow: 0 0 2px rgba(255, 255, 255, 0.8);
}

.tooltip-description {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.2);
}

.tooltip-description p {
  margin: 4px 0;
  color: #000;
  text-shadow: 0 0 2px rgba(255, 255, 255, 0.8);
}

.tooltip-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  justify-content: center;
}

.tooltip-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.tooltip-btn.primary {
  background: #1890ff;
  color: white;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.tooltip-btn.primary:hover {
  background: #40a9ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.tooltip-btn.secondary {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: 1px solid rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
}

.tooltip-btn.secondary:hover {
  background: rgba(255, 255, 255, 1);
  color: #1890ff;
  border-color: #1890ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
  }

  .cat-icon {
    font-size: 1.8rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .apps-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-box {
    width: 100%;
  }

  .search-input {
    flex: 1;
    min-width: auto;
  }
}
</style>
