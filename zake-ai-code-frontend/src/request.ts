import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: 'http://localhost:8123/api',
  // 超时连接
  timeout: 60000,
  // 携带凭证，确保Session能正确传递
  withCredentials: true,
  // 设置请求头
  headers: {
    'Content-Type': 'application/json',
  },
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 后端使用Session认证，不需要手动添加Authorization头
    // withCredentials: true 已经确保Session能正确传递
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        // 清除本地存储的用户信息
        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // 处理网络错误
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          message.error('未授权，请重新登录')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('token')
          window.location.href = '/user/login'
          break
        case 403:
          message.error('权限不足')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      message.error('网络连接失败，请检查网络')
    } else {
      message.error('请求配置错误')
    }
    return Promise.reject(error)
  },
)

export default myAxios
