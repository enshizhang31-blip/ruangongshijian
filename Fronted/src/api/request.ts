import axios from 'axios'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type { Result } from '@/types'
import { getToken, removeToken } from '@/utils/storage'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token && config.headers) {
    config.headers.Authorization = token
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    if (response.data.code === 200) {
      return response.data as unknown as AxiosResponse
    }
    // 401 未授权
    if (response.data.code === 401) {
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(response.data.message || '未登录'))
    }
    return Promise.reject(new Error(response.data.message || '请求失败'))
  },
  (error) => {
    return Promise.reject(error)
  },
)

export default request
