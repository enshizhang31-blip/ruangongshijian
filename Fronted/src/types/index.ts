// ========== 通用 ==========
export interface PageQuery {
  page: number
  pageSize: number
  keyword?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

// ========== 用户 ==========
export interface User {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  status: number
  createTime?: string
}

// ========== 商品 ==========
export interface Product {
  id: number
  name: string
  categoryId?: number
  categoryName?: string
  price: number
  stock: number
  unit?: string
  description?: string
  imageUrl?: string
  status: number
  createTime?: string
}

export interface ProductCategory {
  id: number
  name: string
  parentId?: number
  sort?: number
}

// ========== 客户 ==========
export interface Customer {
  id: number
  name: string
  phone?: string
  email?: string
  address?: string
  customerType: number
  level?: number
  balance?: number
  remark?: string
  status: number
  createTime?: string
}

// ========== 销售 ==========
export interface SaleOrder {
  id: number
  orderNo: string
  customerId: number
  customerName?: string
  salesUserId: number
  salesUserName?: string
  totalAmount: number
  discountAmount: number
  payableAmount: number
  orderStatus: number
  paymentStatus: number
  remark?: string
  createTime?: string
}

export interface SaleOrderItem {
  id: number
  orderId: number
  productId: number
  productName?: string
  price: number
  quantity: number
  subtotal: number
}

// ========== 统计 ==========
export interface DashboardStats {
  todaySales: number
  todayOrders: number
  todayCustomers: number
  totalProducts: number
  lowStockProducts: number
  recentOrders: SaleOrder[]
}
