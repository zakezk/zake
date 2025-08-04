



<template>
  <a-layout-header class="header">
    <div class="logo-container">
      <img src="/favicon.ico" alt="Logo" class="logo" />
      <h1 class="title">zake应用生成</h1>
    </div>

    <a-menu
      mode="horizontal"
      :items="menuItems"
      class="menu"
      :selectedKeys="[selectedKey]"
      @select="(e: any) => handleMenuSelect(e.key)"
    />

    <div class="user-container" v-if="isLoggedIn" @mouseenter="isDropdownOpen = true" @mouseleave="isDropdownOpen = false">
      <img :src="userAvatar" alt="User Avatar" class="avatar" />
      <span class="username">{{ userName }}</span>
      <svg class="dropdown-arrow" :class="{ 'rotated': isDropdownOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="6,9 12,15 18,9"></polyline>
      </svg>
      <div class="dropdown-menu" v-show="isDropdownOpen">
        <button @click="handleLogout" class="logout-btn">
          <svg class="logout-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16,17 21,12 16,7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          <span>注销</span>
        </button>
      </div>
    </div>
    <div class="user-container" v-else>
      <router-link to="/user/login">
        <a-button type="primary">登录</a-button>
      </router-link>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const selectedKey = ref<string>(route.path)
const isDropdownOpen = ref(false)

// 使用computed来响应式获取用户信息
const isLoggedIn = computed(() => {
  return !!loginUserStore.loginUser.userAccount
})

const userName = computed(() => {
  return loginUserStore.loginUser.userAccount || loginUserStore.loginUser.userName || '用户'
})

const userAvatar = computed(() => {
  // 优先用用户自己的头像，只有为空时才用默认头像
  const avatar = loginUserStore.loginUser.userAvatar
  return avatar && avatar.trim() !== '' ? avatar : '/default-avatar.svg'
})

const userRole = computed(() => {
  return loginUserStore.loginUser.userRole || 'user'
})

const isAdmin = computed(() => {
  return userRole.value === 'admin'
})

const handleLogout = async () => {
  try {
    // 调用登出API
    await userLogout()
  } catch (error) {
    console.error('Logout API failed:', error)
  } finally {
    // 清除本地存储和状态
    loginUserStore.clearLoginUser()
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    // 重定向到登录页
    router.push('/user/login')
  }
}

// 初始化时加载用户信息
onMounted(() => {
  // 尝试从localStorage恢复用户信息到状态管理
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const user = JSON.parse(userInfoStr)
      loginUserStore.setLoginUser(user)
    } catch (error) {
      console.error('Error parsing stored user info:', error)
      localStorage.removeItem('userInfo')
    }
  }
})

// 监听路由变化更新选中状态
watch(
  () => route.path,
  (newPath) => {
    selectedKey.value = newPath
  },
  { immediate: true }
)

// 动态菜单配置项
const menuItems = computed(() => {
  const items = [
    { key: '/', label: '首页' },
    { key: '/about', label: '关于' }
  ]
  
  // 只有管理员才显示管理菜单
  if (isAdmin.value) {
    items.push({ key: '/admin/user', label: '用户管理' })
    items.push({ key: '/admin/app', label: '应用管理' })
  }
  
  return items
})

const handleMenuSelect = (key: string) => {
  selectedKey.value = key
  router.push(key)
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 24px;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  margin: 0;
}

.menu {
  flex: 1;
  margin: 0 24px;
}

.user-container {
  display: flex;
  align-items: center;
  position: relative;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.user-container:hover {
  background-color: #f5f5f5;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 6px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12);
  padding: 4px;
  margin-top: 4px;
  min-width: 90px;
  z-index: 100;
  border: 1px solid #f0f0f0;
  animation: dropdownFadeIn 0.2s ease-out;
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-3px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: white;
  font-weight: 500;
  font-size: 12px;
}

.logout-btn:hover {
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
}

.logout-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(255, 77, 79, 0.2);
}

.logout-btn .logout-icon {
  width: 12px;
  height: 12px;
  margin-right: 4px;
  stroke: white;
  stroke-width: 2;
  transition: transform 0.2s ease;
}

.logout-btn:hover .logout-icon {
  transform: translateX(1px);
}

.logout-btn span {
  font-size: 12px;
  color: white;
  font-weight: 500;
}

.logout-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 移除不需要的样式 */
.dropdown-header,
.dropdown-avatar,
.user-info,
.user-name,
.user-role,
.dropdown-divider {
  display: none;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.user-container:hover .avatar {
  border-color: #4096ff;
  transform: scale(1.05);
}

.username {
  color: #333;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.user-container:hover .username {
  color: #4096ff;
}

/* 简化箭头指示器 */
.dropdown-arrow {
  width: 14px;
  height: 14px;
  margin-left: 6px;
  transition: all 0.3s ease;
  transform: rotate(0deg);
  color: #666;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
  color: #4096ff;
}

.user-container:hover .dropdown-arrow {
  color: #4096ff;
}

/* 简化下拉菜单箭头 */
.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -3px;
  right: 12px;
  width: 6px;
  height: 6px;
  background: white;
  border: 1px solid #f0f0f0;
  border-bottom: none;
  border-right: none;
  transform: rotate(45deg);
  z-index: -1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dropdown-menu {
    min-width: 80px;
    right: -3px;
  }
  
  .logout-btn {
    padding: 4px 8px;
    font-size: 11px;
  }
  
  .logout-btn .logout-icon {
    width: 10px;
    height: 10px;
    margin-right: 3px;
  }
}
</style>
