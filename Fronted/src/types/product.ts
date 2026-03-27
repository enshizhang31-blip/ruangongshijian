// ========== 商品 ==========
export interface Product {
    id: number
    name: string
    categoryId?: number
    categoryName?: string
    price: number
    stock: number
    unit?: string
    description?: string
    imageUrl?: string
    status: number
    createTime?: string
}

export interface ProductCategory {
    id: number
    name: string
    parentId?: number
    sort?: number
}
