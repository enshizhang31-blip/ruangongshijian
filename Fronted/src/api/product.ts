import api from './request'
import type { Product, ProductCategory, PageQuery, PageResult } from '@/types'

export const productApi = {
  list(params: PageQuery) {
    return api.get<PageResult<Product>>('/admin/product', { params })
  },
  getById(id: number) {
    return api.get<Product>(`/admin/product/${id}`)
  },
  create(data: Product) {
    return api.post<Product>('/admin/product', data)
  },
  update(data: Product) {
    return api.put<Product>('/admin/product', data)
  },
  delete(id: number) {
    return api.delete<void>(`/admin/product/${id}`)
  },
  categories() {
    return api.get<ProductCategory[]>('/admin/product/categories')
  },
}
