import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/pages/HomeView.vue'
import AboutView from '@/pages/AboutView.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
    {
      path: '/user/login',
      name: 'UserLogin',
      component: UserLoginPage
    },
    {
      path: '/user/register',
      name: 'UserRegister',
      component: UserRegisterPage
    },
    {
      path: '/admin/user',
      name: 'UserManage',
      component: UserManagePage,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/app',
      name: 'AppManage',
      component: AppManagePage,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/app/chat/:appId',
      name: 'AppChat',
      component: AppChatPage,
      meta: { requiresAuth: true }
    },
    {
      path: '/app/edit/:appId',
      name: 'AppEdit',
      component: AppEditPage,
      meta: { requiresAuth: true }
    },
  ],
})

// 全局消息提示函数
const showGlobalMessage = (message: string, type: 'error' | 'success' = 'error') => {
  // 创建消息元素
  const messageEl = document.createElement('div')
  messageEl.className = `global-message ${type}`
  messageEl.innerHTML = `
    <div style="display: flex; align-items: center; gap: 8px;">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        ${type === 'error' 
          ? '<circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line>'
          : '<polyline points="20,6 9,17 4,12"></polyline>'
        }
      </svg>
      <span>${message}</span>
    </div>
  `
  messageEl.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 16px;
    border-radius: 8px;
    color: white;
    font-size: 14px;
    font-weight: 500;
    z-index: 9999;
    animation: slideIn 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    ${type === 'error' ? 'background-color: #ff4d4f;' : 'background-color: #52c41a;'}
  `
  
  // 添加动画样式
  const style = document.createElement('style')
  style.textContent = `
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
      from { transform: translateX(0); opacity: 1; }
      to { transform: translateX(100%); opacity: 0; }
    }
  `
  document.head.appendChild(style)
  
  document.body.appendChild(messageEl)
  
  // 3秒后自动移除
  setTimeout(() => {
    messageEl.style.animation = 'slideOut 0.3s ease'
    setTimeout(() => {
      if (messageEl.parentNode) {
        messageEl.parentNode.removeChild(messageEl)
      }
    }, 300)
  }, 3000)
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('userInfo')
  const userInfo = localStorage.getItem('userInfo')
  let userRole = 'user'

  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      userRole = user.userRole || 'user'
    } catch (e) {
      console.error('Failed to parse user info:', e)
    }
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    showGlobalMessage('请先登录后再访问此页面', 'error')
    next('/user/login')
    return
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && userRole !== 'admin') {
    showGlobalMessage('您没有权限访问管理员页面，请联系管理员', 'error')
    next('/user/login')
    return
  }

  // 已登录用户访问登录/注册页面时重定向到首页
  if (isLoggedIn && (to.path === '/user/login' || to.path === '/user/register')) {
    next('/')
    return
  }

  next()
})

export default router
