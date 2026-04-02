// ========== 统计 ==========
import type { SaleOrder } from './sale'

export interface DashboardStats {
    todaySales: number
    todayOrders: number
    todayCustomers: number
    totalProducts: number
    lowStockProducts: number
    recentOrders?: SaleOrder[]
}
