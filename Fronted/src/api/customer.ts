import api from './request'
import type { Customer, PageQuery, PageResult } from '@/types'

export const customerApi = {
  list(params: PageQuery) {
    return api.get<PageResult<Customer>>('/admin/customer', { params })
  },
  getById(id: number) {
    return api.get<Customer>(`/admin/customer/${id}`)
  },
  create(data: Customer) {
    return api.post<Customer>('/admin/customer', data)
  },
  update(data: Customer) {
    return api.put<Customer>('/admin/customer', data)
  },
  delete(id: number) {
    return api.delete<void>(`/admin/customer/${id}`)
  },
}
