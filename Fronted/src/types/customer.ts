// ========== 客户 ==========
export interface Customer {
    id: number
    name: string
    phone?: string
    email?: string
    address?: string
    customerType: number
    level?: number
    balance?: number
    remark?: string
    status: number
    createTime?: string
}
