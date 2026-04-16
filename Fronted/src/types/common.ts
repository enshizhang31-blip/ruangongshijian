// ========== 通用 ==========
import { type Method } from 'axios'

export interface PageQuery {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
  categoryId?: number
}

export interface Pagination {
  page: number
  pageSize: number
  total: number
}

export interface PageResult<T> {
  list: T[]
  pagination: Pagination
}

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

// ========== API 客户端 ==========
export interface ApiRequestConfig {
  method: Method
  url?: string
  params?: unknown
  data?: unknown
  headers?: Record<string, string>
  [key: string]: unknown
}

export interface ApiClient {
  request<T>(config: ApiRequestConfig): Promise<T>
  get<T>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>): Promise<T>
  post<T>(url: string, data?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'data'>): Promise<T>
  put<T>(url: string, data?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'data'>): Promise<T>
  delete<T>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>): Promise<T>
}
