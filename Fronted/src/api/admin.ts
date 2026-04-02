import request from './request'
import type { AdminUser, Department, Role, PageQuery, PageResult } from '@/types'

export const adminApi = {
    // 员工列表
    list(params: PageQuery) {
        return request.get<PageResult<AdminUser>>('/admin/user', { params }).then((res: any) => res.data.data)
    },

    // 获取员工详情
    getById(id: number) {
        return request.get<AdminUser>(`/admin/user/${id}`).then((res: any) => res.data.data)
    },

    // 创建员工
    create(data: Partial<AdminUser>) {
        return request.post<AdminUser>('/admin/user', data).then((res: any) => res.data.data)
    },

    // 更新员工
    update(data: Partial<AdminUser>) {
        return request.put<AdminUser>('/admin/user', data).then((res: any) => res.data.data)
    },

    // 删除员工
    delete(id: number) {
        return request.delete<void>(`/admin/user/${id}`).then((res: any) => res.data.data)
    },

    // 重置密码
    resetPassword(id: number) {
        return request.post<void>(`/admin/user/${id}/resetpwd`).then((res: any) => res.data.data)
    },
}

export const departmentApi = {
    // 部门列表
    list() {
        return request.get<Department[]>('/admin/department').then((res: any) => res.data.data)
    },

    // 获取部门详情
    getById(id: number) {
        return request.get<Department>(`/admin/department/${id}`).then((res: any) => res.data.data)
    },

    // 创建部门
    create(data: Partial<Department>) {
        return request.post<Department>('/admin/department', data).then((res: any) => res.data.data)
    },

    // 更新部门
    update(data: Partial<Department>) {
        return request.put<Department>('/admin/department', data).then((res: any) => res.data.data)
    },

    // 删除部门
    delete(id: number) {
        return request.delete<void>(`/admin/department/${id}`).then((res: any) => res.data.data)
    },
}

export const roleApi = {
    // 角色列表
    list(params: PageQuery) {
        return request.get<PageResult<Role>>('/admin/role', { params }).then((res: any) => res.data.data)
    },

    // 获取角色详情
    getById(id: number) {
        return request.get<Role>(`/admin/role/${id}`).then((res: any) => res.data.data)
    },

    // 获取所有角色（不带分页）
    all() {
        return request.get<Role[]>('/admin/role/all').then((res: any) => res.data.data)
    },
}
