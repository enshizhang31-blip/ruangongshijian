// ========== 客户 ==========
export interface Customer {
  id: number
  openid?: string
  nickname?: string
  avatar?: string
  phone?: string
  memberLevel?: number
  balance?: number
  points?: number
  totalConsume?: number
  totalPoints?: number
  status: number
  createdAt?: string
  updatedAt?: string
}
