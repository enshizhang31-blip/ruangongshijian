import axios from 'axios'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type { Result } from '@/types'
import { getToken, removeToken } from '@/utils/storage'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token && config.headers) {
    config.headers.Authorization = token
  }
  return config
})

// 分离响应拦截器函数，避免类型推导冲突
function onResponseSuccess(response: AxiosResponse<Result>): unknown {
  const res = response.data
  if (res.code === 200) {
    return res.data
  }
  if (res.code === 401) {
    removeToken()
    router.push('/login')
    return Promise.reject(new Error(res.message || '未登录'))
  }
  return Promise.reject(new Error(res.message || '请求失败'))
}

function onResponseError(error: unknown): Promise<never> {
  return Promise.reject(error)
}

request.interceptors.response.use(onResponseSuccess as any, onResponseError as any)

export default request
