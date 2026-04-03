// ========== SN码 ==========
export interface SnCode {
  id: number
  snCode: string
  skuId?: number
  spuId?: number
  spuName?: string
  skuCode?: string
  specJson?: string
  price?: number
  status: number // 0:在库 1:已售 2:已作废 3:退货中 4:已退货
  source?: number // 1:手动 2:CSV 3:自动生成
  createdAt?: string
  soldAt?: string
  updatedAt?: string
}

export interface SnCodeLog {
  id: number
  snCodeId?: number
  snCode?: string
  skuId?: number
  operation?: string // 录入, 销售, 解绑
  fromStatus?: number
  toStatus?: number
  operatorId?: number
  operatorName?: string
  remark?: string
  createdAt?: string
}

export interface SnQuery {
  sn?: string
  goodsId?: number
  skuId?: number
  status?: number
  source?: number
  startDate?: string
  endDate?: string
}
