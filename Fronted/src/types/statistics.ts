// ========== 统计 ==========

export interface DashboardStats {
    todaySales: number
    monthSales: number
    totalSales: number
    todayOrders: number
    monthOrders: number
    totalOrders: number
    totalCustomers: number
    totalProducts: number
    totalSnCodes: number
    todayCustomers?: number
    lowStockProducts?: number
}
