<template>
  <div class="login-container">
    <div class="login-card">
      <h2>zake-Ai-应用生成</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">

          <input
            type="text"
            id="username"
            v-model="form.userAccount"
            required
            placeholder="请输入用户名"
          />
        </div>
        <div class="form-group">
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="form.userPassword"
              required
              placeholder="请输入密码"
            />
            <button 
              type="button" 
              class="password-toggle-btn"
              @click="togglePassword"
              :title="showPassword ? '隐藏密码' : '显示密码'"
            >
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
            </button>
          </div>
        </div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        <div class="form-actions">
          <button type="submit" class="login-button" :disabled="loading">
            <span v-if="loading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </div>
        <div class="form-links">
          <router-link to="/user/register">还没有账号？去注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { userLogin } from '@/api/userController';
import { useLoginUserStore } from '@/stores/loginUser';

interface LoginForm {
  userAccount: string;
  userPassword: string;
}

const form = reactive<LoginForm>({
  userAccount: '',
  userPassword: ''
})

const loading = ref(false);
const error = ref('');
const success = ref('');
const showPassword = ref(false);
const router = useRouter();
const route = useRoute();
const loginUserStore = useLoginUserStore();

// 切换密码显示/隐藏
const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

// 检查是否从注册页面跳转过来
onMounted(() => {
  if (route.query.from === 'register') {
    success.value = '注册成功！请登录您的账号';
    setTimeout(() => {
      success.value = '';
    }, 500);
  }
});

const handleLogin = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const response = await userLogin(form);
    // 验证响应数据有效性
    if (!response || !response.data || typeof response.data !== 'object') {
      throw new Error('无效的用户数据格式');
    }
    
    // 检查响应状态
    if (response.data.code !== 0) {
      throw new Error(response.data.message || '登录失败');
    }
    
    // 确保存储的用户信息包含基本字段
    // 处理可能的嵌套用户对象结构
    const userData = response.data.data || response.data;
    const userInfo = {
      userAccount: (userData as any).userAccount || form.userAccount,
      userAvatar: (userData as any).userAvatar || '',
      ...userData
    };
    
    // 存储token（如果API返回了token）
    if ((userData as any).token) {
      localStorage.setItem('token', (userData as any).token);
    }
    
    // 存储到localStorage和状态管理
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
    loginUserStore.setLoginUser(userInfo);
    
    console.log('Stored user info:', userInfo);
    router.push('/?from=login');
  } catch (err) {
    error.value = '用户名或密码错误，请重试';
    console.error('Login failed:', err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-card {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: #4096ff;
}

.password-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-container input {
  padding-right: 40px;
}

.password-toggle-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #666;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle-btn:hover {
  background-color: #f5f5f5;
  color: #4096ff;
}

.password-toggle-btn svg {
  width: 16px;
  height: 16px;
  stroke: currentColor;
  stroke-width: 2;
  fill: none;
}

.login-button {
  width: 100%;
  padding: 0.8rem;
  background-color: #4096ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover:not(:disabled) {
  background-color: #3086e8;
}

.login-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.form-links {
  margin-top: 1rem;
  text-align: center;
}

.form-links a {
  color: #4096ff;
  text-decoration: none;
}

.form-links a:hover {
  text-decoration: underline;
}

.error-message {
  color: #ff4d4f;
  margin-bottom: 1rem;
  text-align: center;
}

.success-message {
  color: #52c41a;
  margin-bottom: 1rem;
  text-align: center;
}
</style>
