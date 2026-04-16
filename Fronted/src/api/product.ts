import api from './request'
import type { Product, ProductCategory, Sku, Spec, SpecValue, PageQuery, PageResult } from '@/types'

export const productApi = {
  // ========== SPU ==========
  list(params: PageQuery & { categoryId?: number; status?: number }) {
    return api.get<{ list: Product[]; pagination: { page: number; pageSize: number; total: number } }>('/admin/spu', { params })
  },
  getById(id: number) {
    return api.get<Product>(`/admin/spu/${id}`)
  },
  create(data: Partial<Product>) {
    return api.post<Product>('/admin/spu', data)
  },
  update(data: Partial<Product> & { id: number }) {
    return api.put<void>('/admin/spu', data)
  },
  delete(id: number) {
    return api.delete<void>(`/admin/spu/${id}`)
  },
  updateStatus(id: number, status: number) {
    return api.put<void>(`/admin/spu/${id}/status`, { status })
  },

  // ========== SKU ==========
  getSkus(spuId: number) {
    return api.get<Sku[]>(`/admin/spu/${spuId}/sku`)
  },
  createSku(data: Partial<Sku>) {
    return api.post<Sku>('/admin/sku', data)
  },
  updateSku(data: Partial<Sku> & { id: number }) {
    return api.put<void>('/admin/sku', data)
  },
  deleteSku(id: number) {
    return api.delete<void>(`/admin/sku/${id}`)
  },

  // ========== 分类 ==========
  categories() {
    return api.get<ProductCategory[]>('/admin/category')
  },
  createCategory(data: Partial<ProductCategory>) {
    return api.post<void>('/admin/category', data)
  },
  updateCategory(id: number, data: Partial<ProductCategory>) {
    return api.put<void>(`/admin/category/${id}`, data)
  },
  deleteCategory(id: number) {
    return api.delete<void>(`/admin/category/${id}`)
  },

  // ========== 规格 ==========
  getSpecs() {
    return api.get<Spec[]>('/admin/spec')
  },
  createSpec(data: { name: string }) {
    return api.post<void>('/admin/spec', data)
  },
  updateSpec(id: number, data: { name: string }) {
    return api.put<void>(`/admin/spec/${id}`, data)
  },
  deleteSpec(id: number) {
    return api.delete<void>(`/admin/spec/${id}`)
  },
  getSpecValues(specId: number) {
    return api.get<SpecValue[]>(`/admin/spec/${specId}/value`)
  },
  createSpecValue(specId: number, data: { value: string; sort?: number }) {
    return api.post<void>(`/admin/spec/${specId}/value`, data)
  },
  updateSpecValue(id: number, data: { value?: string; sort?: number }) {
    return api.put<void>(`/admin/spec/value/${id}`, data)
  },
  deleteSpecValue(id: number) {
    return api.delete<void>(`/admin/spec/value/${id}`)
  },
}
