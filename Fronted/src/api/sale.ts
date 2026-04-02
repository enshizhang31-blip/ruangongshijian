import request from './request'
import type { SaleOrder, SaleOrderItem, PageQuery, PageResult } from '@/types'

export const saleApi = {
  list(params: PageQuery) {
    return request.get<PageResult<SaleOrder>>('/admin/sale/order', { params }).then((res: any) => res.data.data)
  },
  getById(id: number) {
    return request.get<SaleOrder>(`/admin/sale/order/${id}`).then((res: any) => res.data.data)
  },
  getItems(orderId: number) {
    return request.get<SaleOrderItem[]>(`/admin/sale/order/${orderId}/items`).then((res: any) => res.data.data)
  },
  create(data: SaleOrder) {
    return request.post<SaleOrder>('/admin/sale/order', data).then((res: any) => res.data.data)
  },
  update(data: SaleOrder) {
    return request.put<SaleOrder>('/admin/sale/order', data).then((res: any) => res.data.data)
  },
  delete(id: number) {
    return request.delete<void>(`/admin/sale/order/${id}`).then((res: any) => res.data.data)
  },
}
