import request from './request'
import type { SaleOrder, SaleOrderItem, PageQuery, PageResult } from '@/types'

export const saleApi = {
  list(params: PageQuery) {
    return request.get<PageResult<SaleOrder>>('/sale/order', { params }).then((res) => res.data)
  },
  getById(id: number) {
    return request.get<SaleOrder>(`/sale/order/${id}`).then((res) => res.data)
  },
  getItems(orderId: number) {
    return request.get<SaleOrderItem[]>(`/sale/order/${orderId}/items`).then((res) => res.data)
  },
  create(data: SaleOrder) {
    return request.post<SaleOrder>('/sale/order', data).then((res) => res.data)
  },
  update(data: SaleOrder) {
    return request.put<SaleOrder>('/sale/order', data).then((res) => res.data)
  },
  delete(id: number) {
    return request.delete<void>(`/sale/order/${id}`).then((res) => res.data)
  },
}
