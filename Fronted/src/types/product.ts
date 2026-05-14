// ========== 商品 ==========
export interface Product {
  id: number
  name: string
  categoryId?: number
  categoryName?: string
  brand?: string
  imageUrl?: string
  images?: string
  shortDesc?: string
  description?: string
  status: number // 0:下架 1:上架
  skuCount?: number
  stockCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface Sku {
  id: number
  spuId: number
  skuCode: string
  specJson?: string
  price: number
  costPrice?: number
  unit?: string
  imageUrl?: string
  status: number
  stock?: number
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
  children?: ProductCategory[]
  createdAt?: string
  updatedAt?: string
}

export interface Spec {
  id: number
  name: string
  values?: SpecValue[]
}

export interface SpecValue {
  id: number
  specId: number
  value: string
  sort?: number
}

// 批量生成SKU参数
export interface BatchGenerateSkuParams {
  spuId: number
  specIds: number[]
  codePrefix?: string
  defaultPrice?: number
  defaultCostPrice?: number
}
