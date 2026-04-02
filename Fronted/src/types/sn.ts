// ========== SN码 ==========
export interface SnCode {
    id: number
    sn: string
    goodsId: number
    goodsName?: string
    skuId?: number
    status: number // 0:未绑定 1:已绑定 2:已使用 3:已退货
    bindTime?: string
    usedTime?: string
    orderId?: number
    remark?: string
    createTime?: string
}

export interface SnCodeLog {
    id: number
    snId: number
    sn?: string
    operationType: number // 1:录入 2:绑定 3:解绑 4:使用 5:退货
    operationTypeName?: string
    operatorId: number
    operatorName?: string
    content?: string
    createTime?: string
}

export interface SnQuery {
    sn?: string
    goodsId?: number
    status?: number
    startDate?: string
    endDate?: string
}
