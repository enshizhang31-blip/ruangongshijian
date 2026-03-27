import request from './request'
import type { SaleOrder, SaleOrderItem, PageQuery, PageResult } from '@/types'

export const saleApi = {
  list(params: PageQuery) {
    return request.get<PageResult<SaleOrder>>('/sale/order', { params })
  },
  getById(id: number) {
    return request.get<SaleOrder>(`/sale/order/${id}`)
  },
  getItems(orderId: number) {
    return request.get<SaleOrderItem[]>(`/sale/order/${orderId}/items`)
  },
  create(data: SaleOrder) {
    return request.post<SaleOrder>('/sale/order', data)
  },
  update(data: SaleOrder) {
    return request.put<SaleOrder>('/sale/order', data)
  },
  delete(id: number) {
    return request.delete<void>(`/sale/order/${id}`)
  },
}
