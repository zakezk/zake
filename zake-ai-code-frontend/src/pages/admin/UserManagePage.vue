<template>
  <div class="admin-user-manage">
    <div class="header-section">
      <h1>用户管理</h1>
      <button @click="showAddUserModal = true" class="add-user-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        添加用户
      </button>
    </div>

    <div class="search-bar">
      <input type="text" v-model="searchKeyword" placeholder="搜索用户名..." @keyup.enter="fetchUsers">
      <button @click="fetchUsers" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>

    <div class="table-container">
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>头像</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>简介</th>
            <th>角色</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="loading-row">
            <td colspan="9" class="loading-cell">
              <div class="loading-spinner"></div>
              加载中...
            </td>
          </tr>
          <tr v-else-if="users.length === 0" class="empty-row">
            <td colspan="9" class="empty-cell">
              暂无用户数据
            </td>
          </tr>
          <tr v-else v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td class="avatar-cell">
              <img :src="getUserAvatar(user.userAvatar)" :alt="user.userName" class="user-avatar">
            </td>
            <td>{{ user.userAccount }}</td>
            <td>{{ user.userName || '-' }}</td>
            <td class="profile-cell">
              <span class="profile-text" :title="user.userProfile || '暂无简介'">
                {{ user.userProfile || '暂无简介' }}
              </span>
            </td>
            <td>
              <span :class="getRoleClass(user.userRole)">{{ getRoleText(user.userRole) }}</span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>{{ formatDate(user.updateTime) }}</td>
            <td class="action-buttons">
              <button @click="editUser(user)" class="edit-btn" title="编辑">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button @click="deleteUser(user.id)" class="delete-btn" title="删除">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3,6 5,6 21,6"></polyline>
                  <path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2V6"></path>
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination-container">
      <div class="pagination-info">
        <span class="total-count">共 {{ totalCount || 0 }} 条</span>
      </div>
      
      <div class="pagination-controls">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 1"
          class="page-btn"
          :class="{ disabled: currentPage === 1 }"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 18l-6-6 6-6"/>
          </svg>
        </button>
        
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            @click="changePage(page)"
            class="page-btn"
            :class="{ active: page === currentPage }"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage === totalPages"
          class="page-btn"
          :class="{ disabled: currentPage === totalPages }"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </button>
      </div>
      
      <div class="page-size-selector">
        <select v-model="pageSize" @change="changePageSize" class="page-size-select">
          <option value="5">5 / page</option>
          <option value="10">10 / page</option>
          <option value="20">20 / page</option>
          <option value="50">50 / page</option>
        </select>
      </div>
    </div>

    <!-- 添加用户模态框 -->
    <div v-if="showAddUserModal" class="modal-overlay" @click="showAddUserModal = false">
      <div class="modal-content" @click.stop>
        <h3>添加用户</h3>
        <form @submit.prevent="addUser">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="addUserForm.userAccount" type="text" required>
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="addUserForm.userName" type="text">
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="addUserForm.userPassword" type="password" required>
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="addUserForm.userRole">
              <option value="user">普通用户</option>
              <option value="admin">管理员</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showAddUserModal = false" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">添加</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div v-if="showEditUserModal" class="modal-overlay" @click="showEditUserModal = false">
      <div class="modal-content" @click.stop>
        <h3>编辑用户</h3>
        <form @submit.prevent="updateUser">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="editUserForm.userAccount" type="text" disabled>
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="editUserForm.userName" type="text">
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="editUserForm.userRole">
              <option value="user">普通用户</option>
              <option value="admin">管理员</option>
            </select>
          </div>
          <div class="form-group">
            <label>个人简介</label>
            <textarea v-model="editUserForm.userProfile" rows="3"></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showEditUserModal = false" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">保存</button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="message" :class="['message-toast', message.includes('成功') ? 'success' : 'error']">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { listUserVoByPage, deleteUser as deleteUserApi, addUser as addUserApi, updateUser as updateUserApi } from '@/api/userController';

interface User {
  id?: string;
  userAccount?: string;
  userName?: string;
  userRole?: string;
  userProfile?: string;
  userAvatar?: string;
  isDelete?: number;
  createTime?: string;
  updateTime?: string;
}

const users = ref<User[]>([]);
const searchKeyword = ref('');
const currentPage = ref(1);
const totalPages = ref(1);
const totalCount = ref(0);
const pageSize = ref(10);
const showAddUserModal = ref(false);
const showEditUserModal = ref(false);
const loading = ref(false);
const message = ref('');

// 添加用户表单
const addUserForm = ref({
  userAccount: '',
  userName: '',
  userPassword: '',
  userRole: 'user'
});

// 编辑用户表单
const editUserForm = ref({
  id: '',
  userAccount: '',
  userName: '',
  userRole: 'user',
  userProfile: ''
});

import { computed } from 'vue';

const showMessage = (msg: string, type: 'success' | 'error' = 'success') => {
  message.value = msg;
  setTimeout(() => {
    message.value = '';
  }, 3000);
};

// 计算可见的页码
const visiblePages = computed(() => {
  const pages = [];
  const maxVisible = 5;
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages.value, start + maxVisible - 1);
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1);
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await listUserVoByPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      userAccount: searchKeyword.value || undefined
    });
    if (response.data && response.data.data) {
      users.value = response.data.data.records || [];
      totalPages.value = response.data.data.totalPage || 1;
      totalCount.value = response.data.data.totalRow || 0;
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    showMessage('获取用户列表失败', 'error');
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  searchKeyword.value = '';
  currentPage.value = 1;
  fetchUsers();
};

const editUser = (user: User) => {
  editUserForm.value = {
    id: user.id || 0,
    userAccount: user.userAccount || '',
    userName: user.userName || '',
    userRole: user.userRole || 'user',
    userProfile: user.userProfile || ''
  };
  showEditUserModal.value = true;
};

const deleteUser = async (id: number | undefined) => {
  if (!id) return;
  
  if (confirm('确定要删除此用户吗?')) {
    try {
      await deleteUserApi({ id });
      showMessage('用户删除成功');
      fetchUsers();
    } catch (error) {
      console.error('删除用户失败:', error);
      showMessage('删除用户失败', 'error');
    }
  }
};

const addUser = async () => {
  try {
    await addUserApi(addUserForm.value);
    showAddUserModal.value = false;
    showMessage('用户添加成功');
    // 重置表单
    addUserForm.value = {
      userAccount: '',
      userName: '',
      userPassword: '',
      userRole: 'user'
    };
    fetchUsers();
  } catch (error) {
    console.error('添加用户失败:', error);
    showMessage('添加用户失败', 'error');
  }
};

const updateUser = async () => {
  try {
    await updateUserApi(editUserForm.value);
    showEditUserModal.value = false;
    showMessage('用户信息更新成功');
    fetchUsers();
  } catch (error) {
    console.error('更新用户失败:', error);
    showMessage('更新用户失败', 'error');
  }
};

const changePage = (page: number) => {
  currentPage.value = page;
  fetchUsers();
};

const changePageSize = () => {
  currentPage.value = 1; // 重置到第一页
  fetchUsers();
};

const getRoleClass = (role?: string) => {
  return role === 'admin' ? 'role-admin' : 'role-user';
};

const getRoleText = (role?: string) => {
  return role === 'admin' ? '管理员' : '普通用户';
};

const formatDate = (dateString?: string) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

function getUserAvatar(avatar?: string) {
  return avatar && avatar.trim() !== '' ? avatar : '/default-avatar.svg';
}

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.admin-user-manage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-section h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.add-user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(66, 185, 131, 0.2);
}

.add-user-btn:hover {
  background-color: #359469;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(66, 185, 131, 0.3);
}

.add-user-btn svg {
  width: 14px;
  height: 14px;
  stroke-width: 2.5;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-bar input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.search-bar input:focus {
  outline: none;
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.1);
}

.search-bar button {
  padding: 8px 14px;
  background-color: #4096ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.search-bar button:hover {
  background-color: #359469;
  transform: translateY(-1px);
}

.reset-btn {
  background-color: #ff4d4f;
}

.reset-btn:hover {
  background-color: #ff7875;
}

.table-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-table th, .user-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.user-table th {
  background-color: #fafafa;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.user-table tr:hover {
  background-color: #f8f9fa;
}

.user-table tr:last-child td {
  border-bottom: none;
}

.avatar-cell {
  width: 60px;
  text-align: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
  transition: transform 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.profile-cell {
  max-width: 200px;
}

.profile-text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
  color: #666;
  font-size: 13px;
}

.role-admin {
  color: #1890ff;
  background-color: #e6f7ff;
  padding: 4px 8px;
  border-radius: 4px;
}

.role-user {
  color: #52c41a;
  background-color: #e6f7ee;
  padding: 4px 8px;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
}

.pagination-info {
  display: flex;
  align-items: center;
}

.total-count {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #d9d9d9;
  background-color: white;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.page-btn:hover:not(.disabled) {
  border-color: #4096ff;
  color: #4096ff;
}

.page-btn.active {
  background-color: #4096ff;
  border-color: #4096ff;
  color: white;
}

.page-btn.disabled {
  cursor: not-allowed;
  opacity: 0.5;
  color: #d9d9d9;
}

.page-btn svg {
  width: 16px;
  height: 16px;
}

.page-size-selector {
  display: flex;
  align-items: center;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background-color: white;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s ease;
}

.page-size-select:focus {
  border-color: #4096ff;
}

.modal-overlay {
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
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  position: relative;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.modal-actions .cancel-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions .cancel-btn:hover {
  background-color: #ff7875;
}

.modal-actions .submit-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions .submit-btn:hover {
  background-color: #359469;
}

.message-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background-color: #4096ff;
  color: white;
  padding: 15px 25px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  z-index: 10000;
  opacity: 0.9;
  transition: opacity 0.5s ease-in-out;
}

.message-toast.success {
  background-color: #52c41a;
}

.message-toast.error {
  background-color: #ff4d4f;
}

.loading-row {
  text-align: center;
}

.loading-cell {
  padding: 40px !important;
  color: #666;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #4096ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-row {
  text-align: center;
}

.empty-cell {
  padding: 40px !important;
  color: #999;
  font-style: italic;
}

.edit-btn, .delete-btn {
  padding: 6px;
  border-radius: 6px;
  cursor: pointer;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.edit-btn svg, .delete-btn svg {
  width: 14px;
  height: 14px;
  stroke-width: 2;
}

.edit-btn {
  background-color: #4096ff;
  color: white;
}

.edit-btn:hover {
  background-color: #359469;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 150, 255, 0.3);
}

.delete-btn {
  background-color: #ff4d4f;
  color: white;
}

.delete-btn:hover {
  background-color: #ff7875;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
}
</style>