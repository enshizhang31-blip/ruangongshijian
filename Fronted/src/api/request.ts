import axios, { type InternalAxiosRequestConfig } from 'axios'
import { getToken, removeToken } from '@/utils/storage'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 只处理错误和登录跳转，不改变成功响应的结构
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 处理业务错误
    if (res.code !== 200) {
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 返回完整响应，让调用者自己提取 data
    return response
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default request
