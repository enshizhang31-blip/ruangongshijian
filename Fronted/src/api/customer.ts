import request from './request'
import type { Customer, PageQuery, PageResult } from '@/types'

export const customerApi = {
  list(params: PageQuery) {
    return request.get<PageResult<Customer>>('/admin/customer', { params }).then((res: any) => res.data.data)
  },
  getById(id: number) {
    return request.get<Customer>(`/admin/customer/${id}`).then((res: any) => res.data.data)
  },
  create(data: Customer) {
    return request.post<Customer>('/admin/customer', data).then((res: any) => res.data.data)
  },
  update(data: Customer) {
    return request.put<Customer>('/admin/customer', data).then((res: any) => res.data.data)
  },
  delete(id: number) {
    return request.delete<void>(`/admin/customer/${id}`).then((res: any) => res.data.data)
  },
}
