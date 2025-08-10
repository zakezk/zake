import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    id: '',
    userAccount: '',
    userName: '未登录',
    userAvatar: '',
    userProfile: '',
    userRole: '',
    createTime: '',
    updateTime: '',
  })

  // 获取登录用户信息
  async function fetchLoginUser() {
    try {
      const res = await getLoginUser()
      if (res && res.data && res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } catch (error) {
      console.error('Failed to fetch login user:', error)
      // 如果获取失败，尝试从localStorage获取
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        try {
          loginUser.value = JSON.parse(storedUser)
        } catch (e) {
          console.error('Failed to parse stored user info:', e)
        }
      }
    }
  }

  // 更新登录用户信息
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser
  }

  // 清除登录用户信息
  function clearLoginUser() {
    loginUser.value = {
      id: '',
      userAccount: '',
      userName: '未登录',
      userAvatar: '',
      userProfile: '',
      userRole: '',
      createTime: '',
      updateTime: '',
    }
    localStorage.removeItem('userInfo')
  }

  return { loginUser, setLoginUser, fetchLoginUser, clearLoginUser }
})
