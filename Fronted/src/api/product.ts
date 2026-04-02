import request from './request'
import type { Product, ProductCategory, PageQuery, PageResult } from '@/types'

export const productApi = {
  list(params: PageQuery) {
    return request.get<PageResult<Product>>('/product', { params }).then((res: any) => res.data.data)
  },
  getById(id: number) {
    return request.get<Product>(`/product/${id}`).then((res: any) => res.data.data)
  },
  create(data: Product) {
    return request.post<Product>('/product', data).then((res: any) => res.data.data)
  },
  update(data: Product) {
    return request.put<Product>('/product', data).then((res: any) => res.data.data)
  },
  delete(id: number) {
    return request.delete<void>(`/product/${id}`).then((res: any) => res.data.data)
  },
  categories() {
    return request.get<ProductCategory[]>('/product/categories').then((res: any) => res.data.data)
  },
}
