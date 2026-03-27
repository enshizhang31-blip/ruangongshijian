import request from './request'
import type { Customer, PageQuery, PageResult } from '@/types'

export const customerApi = {
  list(params: PageQuery) {
    return request.get<PageResult<Customer>>('/customer', { params })
  },
  getById(id: number) {
    return request.get<Customer>(`/customer/${id}`)
  },
  create(data: Customer) {
    return request.post<Customer>('/customer', data)
  },
  update(data: Customer) {
    return request.put<Customer>('/customer', data)
  },
  delete(id: number) {
    return request.delete<void>(`/customer/${id}`)
  },
}
