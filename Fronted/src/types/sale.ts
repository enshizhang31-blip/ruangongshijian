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
