// ========== 商品 ==========
export interface Product {
  id: number
  name: string
  categoryId?: number
  brand?: string
  imageUrl?: string
  images?: string
  description?: string
  status: number // 0:草稿 1:上架 2:下架 3:已删除
  createdAt?: string
  updatedAt?: string
}

export interface ProductCategory {
  id: number
  name: string
  parentId?: number
  icon?: string
  sort?: number
  status?: number
  createdAt?: string
  updatedAt?: string
}
