// ========== 通用 ==========
export interface PageQuery {
  page?: number
  pageSize?: number
  keyword?: string
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
