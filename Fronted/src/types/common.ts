// ========== 通用 ==========
export interface PageQuery {
    page: number
    pageSize: number
    keyword?: string
}

export interface PageResult<T> {
    list: T[]
    total: number
    page: number
    pageSize: number
}

export interface Result<T = unknown> {
    code: number
    message: string
    data: T
}
