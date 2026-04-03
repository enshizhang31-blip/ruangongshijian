// ========== 销售 ==========
export interface SaleOrder {
  id: number
  orderNo: string
  customerId: number
  customerName?: string
  customerPhone?: string
  totalAmount: number
  discountAmount: number
  payAmount: number
  payType?: number
  status?: number
  remark?: string
  createdAt?: string
  paidAt?: string
  completedAt?: string
  cancelledAt?: string
  updatedAt?: string
}

export interface SaleOrderItem {
  id: number
  orderId: number
  orderNo?: string
  skuId: number
  spuName?: string
  skuSpec?: string
  skuImage?: string
  price: number
  quantity: number
  subtotal?: number
  createdAt?: string
}
