import request from './request'
import type { Product, ProductCategory, PageQuery, PageResult } from '@/types'

export const productApi = {
  list(params: PageQuery) {
    return request.get<PageResult<Product>>('/admin/product', { params }).then((res: any) => res.data.data)
  },
  getById(id: number) {
    return request.get<Product>(`/admin/product/${id}`).then((res: any) => res.data.data)
  },
  create(data: Product) {
    return request.post<Product>('/admin/product', data).then((res: any) => res.data.data)
  },
  update(data: Product) {
    return request.put<Product>('/admin/product', data).then((res: any) => res.data.data)
  },
  delete(id: number) {
    return request.delete<void>(`/admin/product/${id}`).then((res: any) => res.data.data)
  },
  categories() {
    return request.get<ProductCategory[]>('/admin/product/categories').then((res: any) => res.data.data)
  },
}
