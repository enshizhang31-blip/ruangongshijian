import api from './request'
import type { DashboardStats } from '@/types'

export const dashboardApi = {
  stats() {
    return api.get<DashboardStats>('/admin/dashboard/stats')
  },
}
