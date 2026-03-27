import request from './request'
import type { DashboardStats } from '@/types'

export const dashboardApi = {
  stats() {
    return request.get<DashboardStats>('/dashboard/stats').then((res) => res.data)
  },
}
