// ========== 客户 ==========
export interface Customer {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  avatarUrl?: string
  gender?: number
  birthday?: string
  balance?: number
  points?: number
  levelId?: number
  levelName?: string
  status: number
  lastLoginAt?: string
  createdAt?: string
  updatedAt?: string
}
