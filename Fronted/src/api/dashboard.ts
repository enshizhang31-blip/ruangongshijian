import request from './request'
import type { DashboardStats } from '@/types'

export const dashboardApi = {
  stats() {
    return request.get<DashboardStats>('/admin/dashboard/stats').then((res: any) => res.data.data)
  },
}
