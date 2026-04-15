// ========== 销售 ==========
export interface SaleOrder {
  id: number
  orderNo: string
  customerId: number
  customerName?: string
  receiverPhone?: string
  receiverName?: string
  receiverAddress?: string
  addressId?: number
  totalAmount: number
  discountAmount?: number
  pointsDiscount?: number
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
  snCodeIds?: string
  createdAt?: string
}
