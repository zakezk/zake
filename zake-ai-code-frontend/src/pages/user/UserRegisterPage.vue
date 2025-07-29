<template>
  <div class="register-container">
    <div class="register-card">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">

          <input
            type="text"
            id="userAccount"
            v-model="form.userAccount"
            required
            placeholder="请输入用户名"
          />
        </div>

        <div class="form-group">
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="userPassword"
              v-model="form.userPassword"
              required
              placeholder="请输入密码"
              minlength="6"
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
        <div class="form-group">
          <div class="password-input-container">
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              id="checkPassword"
              v-model="form.checkPassword"
              required
              placeholder="请确认密码"
              minlength="6"
            />
            <button 
              type="button" 
              class="password-toggle-btn"
              @click="toggleConfirmPassword"
              :title="showConfirmPassword ? '隐藏密码' : '显示密码'"
            >
              <svg v-if="showConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
          <button type="submit" class="register-button" :disabled="loading">
            <span v-if="loading">注册中...</span>
            <span v-else>注册</span>
          </button>
        </div>
        <div class="form-links">
          <router-link to="/user/login">已有账号？去登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { userRegister } from '@/api/userController';

interface RegisterForm {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
}

const form = reactive<RegisterForm>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
});

const loading = ref(false);
const error = ref('');
const success = ref('');
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const router = useRouter();

// 监听密码变化，检查两次输入是否一致
watch(
  () => [form.userPassword, form.checkPassword],
  ([newPassword, newCheckPassword]) => {
    if (newPassword && newCheckPassword && newPassword !== newCheckPassword) {
      error.value = '两次输入的密码不一致';
    } else {
      error.value = '';
    }
  },
  { immediate: false }
);

// 切换密码显示/隐藏
const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

const handleRegister = async () => {
  loading.value = true;
  error.value = '';
  success.value = '';

  // 客户端表单验证
  if (form.userPassword !== form.checkPassword) {
    error.value = '两次输入的密码不一致';
    loading.value = false;
    return;
  }

  if (form.userPassword.length < 6) {
    error.value = '密码长度至少6位';
    loading.value = false;
    return;
  }

  try {
    await userRegister(form);
    success.value = '注册成功！即将跳转到登录页面...';
    // 注册成功后跳转到登录页面
    setTimeout(() => {
        router.push('/user/login?from=register');
      }, 1000);
  } catch (err) {
    error.value = '注册失败，请检查输入信息';
    console.error('Register failed:', err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.register-card {
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

.register-button {
  width: 100%;
  padding: 0.8rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.register-button:hover:not(:disabled) {
  background-color: #359469;
}

.register-button:disabled {
  background-color: #a0e5c8;
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
