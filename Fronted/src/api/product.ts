import request from './request'
import type { Product, ProductCategory, PageQuery, PageResult } from '@/types'

export const productApi = {
  list(params: PageQuery) {
    return request.get<PageResult<Product>>('/product', { params })
  },
  getById(id: number) {
    return request.get<Product>(`/product/${id}`)
  },
  create(data: Product) {
    return request.post<Product>('/product', data)
  },
  update(data: Product) {
    return request.put<Product>('/product', data)
  },
  delete(id: number) {
    return request.delete<void>(`/product/${id}`)
  },
  categories() {
    return request.get<ProductCategory[]>('/product/categories')
  },
}
