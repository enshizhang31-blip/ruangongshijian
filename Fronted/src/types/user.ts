// ========== 员工 ==========
export interface AdminUser {
    id: number
    username: string
    realName?: string
    phone?: string
    email?: string
    departmentId?: number
    permissions?: string
    routes?: string
    status: number
    lastLoginAt?: string
    createdAt?: string
}

// ========== 部门 ==========
export interface Department {
    id: number
    name: string
    parentId?: number
    sort?: number
    status: number
    createdAt?: string
    updatedAt?: string
}

// ========== 角色 ==========
export interface Role {
    id: number
    name: string
    code: string
    description?: string
    permissions?: string
    routes?: string
    isPreset?: number
    status: number
    createdAt?: string
    updatedAt?: string
}
