<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { useRoute } from 'vue-router'

const loginUserStore = useLoginUserStore()
const route = useRoute()
const showLoginSuccess = ref(false)

const isLoggedIn = computed(() => {
  return !!loginUserStore.loginUser.userAccount
})

const userName = computed(() => {
  return loginUserStore.loginUser.userAccount || loginUserStore.loginUser.userName || '用户'
})

const userRole = computed(() => {
  return loginUserStore.loginUser.userRole || '普通用户'
})

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
})
</script>

<template>
  <div class="home">
    <h1>欢迎来到 zake-AI-noCode</h1>
    <div class="user-info" v-if="isLoggedIn">
      <p>当前登录用户: {{ userName }}</p>
      <p>用户角色: {{ userRole }}</p>
      <div class="admin-actions" v-if="userRole === '管理员'">
        <router-link to="/admin/user" class="admin-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
          用户管理
        </router-link>
      </div>
    </div>
    
    <div class="features">
      <h2>功能特性</h2>
      <ul>
        <li>用户登录注册</li>
        <li>个人资料管理</li>
        <li>用户管理（管理员）</li>
        <li>响应式设计</li>
      </ul>
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
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
}

.user-info {
  background-color: #f0f8ff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  border-left: 4px solid #4096ff;
}

.admin-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e8e8e8;
}

.admin-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.admin-link:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.admin-link svg {
  width: 16px;
  height: 16px;
}

.features {
  background-color: #f6ffed;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #52c41a;
}

.features h2 {
  color: #333;
  margin-bottom: 15px;
}

.features ul {
  list-style-type: none;
  padding: 0;
}

.features li {
  padding: 8px 0;
  border-bottom: 1px solid #e8e8e8;
}

.features li:last-child {
  border-bottom: none;
}

.features li:before {
  content: "✓";
  color: #52c41a;
  font-weight: bold;
  margin-right: 10px;
}

/* 登录成功弹窗样式 */
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
</style>
