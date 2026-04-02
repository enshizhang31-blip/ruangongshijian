import request from './request'

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
    return request.post<LoginVO>('/admin/auth/login', data).then((res: any) => res.data.data)
  },

  getCurrentUser() {
    return request.get<LoginVO>('/admin/auth/current').then((res: any) => res.data.data)
  },

  getRoutes() {
    return request.get<{ permissions: string[]; routes: string[] }>('/admin/auth/routes').then((res: any) => res.data.data)
  },
}
