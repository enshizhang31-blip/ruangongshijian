import api from './request'

export interface LoginDTO {
  username: string
  password: string
}

export interface LoginVO {
  userId: number
  username: string
  realName: string
  token: string
  permissions: string[]
  routes: string[]
}

export const authApi = {
  login(data: LoginDTO) {
    return api.post<LoginVO>('/admin/auth/login', data)
  },
  getCurrentUser() {
    return api.get<LoginVO>('/admin/auth/current')
  },
  getRoutes() {
    return api.get<{ permissions: string[]; routes: string[] }>('/admin/auth/routes')
  },
}
