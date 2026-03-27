import request from './request'
import type { Customer, PageQuery, PageResult } from '@/types'

export const customerApi = {
  list(params: PageQuery) {
    return request.get<PageResult<Customer>>('/customer', { params }).then((res) => res.data)
  },
  getById(id: number) {
    return request.get<Customer>(`/customer/${id}`).then((res) => res.data)
  },
  create(data: Customer) {
    return request.post<Customer>('/customer', data).then((res) => res.data)
  },
  update(data: Customer) {
    return request.put<Customer>('/customer', data).then((res) => res.data)
  },
  delete(id: number) {
    return request.delete<void>(`/customer/${id}`).then((res) => res.data)
  },
}
