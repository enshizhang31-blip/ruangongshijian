import axios, { type AxiosResponse, AxiosError } from 'axios'
import { getToken, removeToken } from '@/utils/storage'
import router from '@/router'
import { Message } from '@arco-design/web-vue'
import type { ApiRequestConfig, ApiClient, Result } from '@/types/common'

// 创建 axios 实例
const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

export const api: ApiClient = {
  request<T>(config: ApiRequestConfig) {
    return client
      .request<Result<T>, AxiosResponse<Result<T>>, unknown>(config as any)
      .then((response) => response.data.data)
  },
  get<T>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>) {
    return api.request<T>({ ...config, url, method: 'GET' })
  },
  post<T>(url: string, data?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'data'>) {
    return api.request<T>({ ...config, url, data, method: 'POST' })
  },
  put<T>(url: string, data?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'data'>) {
    return api.request<T>({ ...config, url, data, method: 'PUT' })
  },
  delete<T>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>) {
    return api.request<T>({ ...config, url, method: 'DELETE' })
  },
}

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  console.log(`[请求] ${config.method?.toUpperCase()} ${config.url}`, config.params || config.data || '')
  return config
})

// 响应拦截器
client.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    console.log(`[响应] ${response.config.url}`, response.data)
    const res = response.data
    // 处理业务错误
    if (res.code !== 200) {
      console.error('API Error:', res)
      if (res.code === 401) {
        removeToken()
        router.push('/login')
        Message.error('登录已过期，请重新登录')
      } else if (res.code === 403) {
        Message.error('没有权限访问该资源')
      } else if (res.code === 404) {
        Message.error('请求的资源不存在')
      } else {
        Message.error(res.message || `请求失败 (${res.code})`)
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error: AxiosError) => {
    // 网络错误或服务器错误
    console.error(`[响应错误] ${error.config?.url}`, error.response?.data || error.message)
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as { code?: number; message?: string }
      console.error('API Error:', data || error)
      if (status === 401) {
        removeToken()
        router.push('/login')
        Message.error('登录已过期，请重新登录')
      } else if (status === 403) {
        Message.error('没有权限访问该资源')
      } else if (status === 404) {
        Message.error('请求的资源不存在')
      } else if (status >= 500) {
        Message.error('服务器错误，请稍后重试')
      } else {
        Message.error(data?.message || `请求失败 (${status})`)
      }
    } else {
      Message.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default api
