import api from './request'
import type { SaleOrder, SaleOrderItem, PageQuery, PageResult } from '@/types'

export const saleApi = {
  list(params: PageQuery) {
    return api.get<PageResult<SaleOrder>>('/admin/sale/order', { params })
  },
  getById(id: number) {
    return api.get<SaleOrder>(`/admin/sale/order/${id}`)
  },
  getItems(orderId: number) {
    return api.get<SaleOrderItem[]>(`/admin/sale/order/${orderId}/items`)
  },
  create(data: SaleOrder) {
    return api.post<SaleOrder>('/admin/sale/order', data)
  },
  update(data: SaleOrder) {
    return api.put<SaleOrder>('/admin/sale/order', data)
  },
  delete(id: number) {
    return api.delete<void>(`/admin/sale/order/${id}`)
  },
}
