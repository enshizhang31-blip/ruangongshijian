import api from './request'
import type { AdminUser, Department, Role, PageQuery, PageResult } from '@/types'

export const adminApi = {
    list(params: PageQuery) {
        return api.get<PageResult<AdminUser>>('/admin/user', { params })
    },
    getById(id: number) {
        return api.get<AdminUser>(`/admin/user/${id}`)
    },
    create(data: Partial<AdminUser>) {
        return api.post<AdminUser>('/admin/user', data)
    },
    update(data: Partial<AdminUser>) {
        return api.put<AdminUser>('/admin/user', data)
    },
    delete(id: number) {
        return api.delete<void>(`/admin/user/${id}`)
    },
    resetPassword(id: number) {
        return api.post<void>(`/admin/user/${id}/resetpwd`)
    },
}

export const departmentApi = {
    list() {
        return api.get<Department[]>('/admin/department')
    },
    getById(id: number) {
        return api.get<Department>(`/admin/department/${id}`)
    },
    create(data: Partial<Department>) {
        return api.post<Department>('/admin/department', data)
    },
    update(data: Partial<Department>) {
        return api.put<Department>('/admin/department', data)
    },
    delete(id: number) {
        return api.delete<void>(`/admin/department/${id}`)
    },
}

export const roleApi = {
    list(params: PageQuery) {
        return api.get<PageResult<Role>>('/admin/role', { params })
    },
    getById(id: number) {
        return api.get<Role>(`/admin/role/${id}`)
    },
    all() {
        return api.get<Role[]>('/admin/role/all')
    },
}
