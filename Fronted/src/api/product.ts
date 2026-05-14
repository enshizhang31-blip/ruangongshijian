import api from './request'
import type { Product, ProductCategory, Sku, Spec, SpecValue, PageQuery } from '@/types'

export const productApi = {
  // ========== SPU (商品) ==========
  list(params: PageQuery & { categoryId?: number; status?: number }) {
    return api.get<{ list: Product[]; pagination: { page: number; pageSize: number; total: number } }>('/admin/product', { params })
  },
  getById(id: number) {
    return api.get<Product>(`/admin/product/${id}`)
  },
  create(data: Partial<Product>) {
    return api.post<Product>('/admin/product', data)
  },
  update(data: Partial<Product> & { id: number }) {
    return api.put<void>('/admin/product', data)
  },
  delete(id: number) {
    return api.delete<void>(`/admin/product/${id}`)
  },
  updateStatus(id: number, status: number) {
    return api.put<void>(`/admin/product/${id}/status`, { status })
  },

  // ========== SKU ==========
  getSkus(spuId: number) {
    return api.get<Sku[]>(`/admin/product/${spuId}/sku`)
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
    return api.get<ProductCategory[]>('/admin/product/categories')
  },
  createCategory(data: Partial<ProductCategory>) {
    return api.post<void>('/admin/product/category', data)
  },
  updateCategory(id: number, data: Partial<ProductCategory>) {
    return api.put<void>(`/admin/product/category/${id}`, data)
  },
  deleteCategory(id: number) {
    return api.delete<void>(`/admin/product/category/${id}`)
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

  generateMockData(data: {
    goodsCount?: number
    skuPerGoods?: number
    snPerSku?: number
    specCount?: number
    valuesPerSpec?: number
  }) {
    return api.post<Record<string, number | string>>('/admin/product/mock-data', data)
  },
}
